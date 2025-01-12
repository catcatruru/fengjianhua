package org.jetlinks.ipms.ams;

import org.hswebframework.web.authorization.basic.configuration.EnableAopAuthorize;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.logging.aop.EnableAccessLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(
    scanBasePackages = {"org.jetlinks.pro", "org.jetlinks.ipms"},
    exclude = {
        DataSourceAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class,
        RabbitAutoConfiguration.class,
        KafkaAutoConfiguration.class
    })
@EnableCaching
@EnableEasyormRepository({"org.jetlinks.**.entity"})
@EnableAopAuthorize
@EnableAccessLogger
public class AssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetApplication.class, args);
    }

}
