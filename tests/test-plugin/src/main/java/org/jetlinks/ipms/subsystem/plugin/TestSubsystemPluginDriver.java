package org.jetlinks.ipms.subsystem.plugin;

import org.jetlinks.plugin.core.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collections;

public class TestSubsystemPluginDriver implements PluginDriver {
    @Nonnull
    @Override
    public Description getDescription() {
        return Description.of("test",
                              "测试",
                              "测试",
                              Version.platform_2_2,
                              VersionRange.of(Version.platform_2_2, Version.platform_latest),
                              Collections.emptyMap()
        );
    }

    @Nonnull
    @Override
    public PluginType getType() {
        return TestSubsystemPlugin.type;
    }

    @Nonnull
    @Override
    public Mono<? extends Plugin> createPlugin(@Nonnull String pluginId, @Nonnull PluginContext context) {
        return Mono.just(new TestSubsystemPlugin(pluginId, context));
    }
}
