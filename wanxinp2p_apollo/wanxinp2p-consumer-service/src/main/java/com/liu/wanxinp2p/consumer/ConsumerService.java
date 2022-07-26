package com.liu.wanxinp2p.consumer;


import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableDiscoveryClient
@EnableSwagger2
@EnableApolloConfig
@EnableFeignClients(basePackages = {"com.liu.wanxinp2p.consumer.agent"})
@ComponentScan({"com.liu.wanxinp2p.consumer","org.dromara.hmily"})
public class ConsumerService {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerService.class, args);
    }
}