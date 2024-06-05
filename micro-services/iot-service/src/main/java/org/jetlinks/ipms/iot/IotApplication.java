package org.jetlinks.ipms.iot;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.hswebframework.web.authorization.basic.configuration.EnableAopAuthorize;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.logging.aop.EnableAccessLogger;
import org.springdoc.webflux.core.SpringDocWebFluxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication(
    scanBasePackages = {"org.jetlinks.pro","org.jetlinks.ipms"},
    exclude = {
        DataSourceAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class,
        RabbitAutoConfiguration.class,
        KafkaAutoConfiguration.class
    })
@EnableCaching
@EnableEasyormRepository({
    "org.jetlinks.pro.**.entity",
    "org.jetlinks.ipms.**.entity"
})
@EnableAopAuthorize
@EnableAccessLogger
public class IotApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotApplication.class, args);
    }


    @Configuration(proxyBeanMethods = false)
    @OpenAPIDefinition(
        info = @Info(
            title = "IoT Service",
            description = "物联网管理平台接口文档",
            contact = @Contact(name = "admin"),
            version = "2.2.0-SNAPSHOT"
        )
    )
    @AutoConfigureBefore(SpringDocWebFluxConfiguration.class)
    public static class SwaggerConfiguration {

    }
}
