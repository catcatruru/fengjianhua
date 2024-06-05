package org.jetlinks.ipms.subsystem.plugin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.core.message.DerivedMetadataMessage;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.MessageType;
import org.jetlinks.core.message.RepayableDeviceMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.metadata.SimpleDeviceMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.FileType;
import org.jetlinks.core.metadata.types.FloatType;
import org.jetlinks.plugin.core.PluginContext;
import org.jetlinks.plugin.core.PluginType;
import org.jetlinks.plugin.internal.functional.FunctionalPlugin;
import org.jetlinks.plugin.internal.functional.FunctionalService;
import org.jetlinks.sdk.server.SdkServices;
import org.jetlinks.sdk.server.device.cmd.UpstreamCommand;
import org.jetlinks.sdk.server.file.FileInfo;
import org.jetlinks.sdk.server.file.UploadFileCommand;
import org.jetlinks.supports.official.JetLinksDeviceMetadataCodec;
import org.springframework.http.MediaType;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TestSubsystemPlugin extends FunctionalPlugin {

    public static final PluginType type = new PluginType() {
        @Override
        public String getId() {
            return "Test";
        }

        @Override
        public String getName() {
            return "测试";
        }
    };

    private final Disposable.Composite disposable = Disposables.composite();

    private final FunctionalService subsystemDeviceService;
    private final FunctionalService fileService;

    public TestSubsystemPlugin(String id, PluginContext context) {
        super(id, context);

        // subsystemDevice 可能是本地也可能是其他微服务；
        // 也可能当前是在边缘端，subsystemDevice在云端。
        subsystemDeviceService = getService("subsystemDevice");
        fileService = getService(SdkServices.fileService);
    }

    private void startup() {

        //定时推送模拟数据
        disposable.add(
            context()
                .scheduler()
                .interval("publish-device",
                          Mono.defer(this::publishMockData),
                          Duration.ofSeconds(10))
        );

//        SimpleDeviceMetadata metadata = new SimpleDeviceMetadata();
//        metadata.addProperty(SimplePropertyMetadata.of("val", "模拟值", FloatType.GLOBAL));
//        metadata.addProperty(SimplePropertyMetadata.of("screenshot", "屏幕截图", new FileType()
//            .bodyType(FileType.BodyType.url)
//            .mediaType(MediaType.IMAGE_PNG)));
//
//        DerivedMetadataMessage message = new DerivedMetadataMessage();
//        message.setAll(true);
//        message.setMetadata(JetLinksDeviceMetadataCodec.getInstance().doEncode(metadata));
//        message.setDeviceId("subsystem-plugin-test");
//
//        Mono.delay(Duration.ofSeconds(10))
//            .flatMap(ignore -> subsystemDeviceService.execute(new UpstreamCommand().withMessage(message)))
//            .subscribe();
    }

    private Mono<Void> publishMockData() {
        ReportPropertyMessage msg = new ReportPropertyMessage();
        msg.setDeviceId("subsystem-plugin-test");
        msg.setProperties(Collections.singletonMap("val", ThreadLocalRandom
            .current()
            .nextFloat()));

        return subsystemDeviceService
            .execute(new UpstreamCommand().withMessage(msg))
            .as(context()
                    .monitor()
                    .tracer()
                    .traceMono("/upstream-message",
                               (ctx, builder) -> {
                               }))
            .onErrorResume(err -> {
                context().monitor().logger().error("推送设备数据失败: {}", msg, err);
                return Mono.empty();
            });
    }

    //监听从平台下发的指令。
    //插件可以运行在平台的iot-service，其他微服务，或者边缘端。
    @Schema(name = "Downstream")
    @SuppressWarnings("all")
    public Mono<DeviceMessage> downstream(@Schema(name = "message") Object message) {
        if (message instanceof Map) {
//            return Mono.error(new UnsupportedOperationException("测试"));
            message = MessageType
                .convertMessage(((Map) message))
                .orElse(null);
        }

        if (message instanceof FunctionInvokeMessage) {
            FunctionInvokeMessage msg = ((FunctionInvokeMessage) message);
            String funcId = msg.getFunctionId();
            //模拟文件上传
            if ("screenshot".equals(funcId)) {
                return doScreenshot()
                    .map(msg.newReply()::success);
            }
        }
        if (message instanceof ReadPropertyMessage) {
            ReadPropertyMessage msg = ((ReadPropertyMessage) message);
            if (((ReadPropertyMessage) message).getProperties().contains("screenshot")) {
                return doScreenshot()
                    .map(url -> msg.newReply().success(Collections.singletonMap("screenshot", url)));
            }
        }
        if (message instanceof RepayableDeviceMessage) {
            return Mono
                .delay(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1, 3)))
                .<DeviceMessage>thenReturn(((RepayableDeviceMessage<?>) message).newReply().success())
                .as(context()
                        .monitor()
                        .tracer()
                        .traceMono("/handle-downstream"));
        }
        return Mono.empty();
    }

    private Mono<String> doScreenshot() {
        return Mono
            .fromCallable(() -> {
                try (ByteBufOutputStream outputStream = new ByteBufOutputStream(ByteBufAllocator.DEFAULT.buffer())) {
                    // jvm需要增加启动参数 -Djava.awt.headless=false
                    Robot robot = new Robot();
                    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();

                    Rectangle rectangle = new Rectangle(dimension);

                    BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
                    ImageIO.write(bufferedImage, "png", outputStream);
                    outputStream.flush();
                    return outputStream.buffer();
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(imageBuffer -> {
                //访问文件服务
                return UploadFileCommand
                    .execute(fileService,
                             imageBuffer,
                             //单个分片大小
                             200 * 1024,
                             cmd -> {
                                 cmd.withFileName("screenshot.png");
                             })
                    .map(FileInfo::getAccessUrl);
            });
    }

    @Override
    protected Mono<Void> doStart() {
        startup();

        return super.doStart();
    }

    @Override
    protected Mono<Void> doShutdown() {
        disposable.dispose();
        return super.doShutdown();
    }

    @Override
    public PluginType getType() {
        return type;
    }
}
