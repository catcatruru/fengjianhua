package org.jetlinks.project.configuration;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 设备网关配置.
 *
 * @author zhangji 2024/1/15
 */
@Configuration
public class DeviceGatewayConfiguration {

    @ConfigurationProperties(prefix = "vertx")
    @Bean
    public VertxOptions vertxOptions() {
        return new VertxOptions();
    }

    @Bean
    public Vertx vertx(VertxOptions vertxOptions) {
        return Vertx.vertx(vertxOptions);
    }

}
