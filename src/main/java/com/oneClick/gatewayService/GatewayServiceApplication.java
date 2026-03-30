package com.oneClick.gatewayService;

import com.oneClick.gatewayService.resolver.IPKeyResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableDiscoveryClient //use Eureka
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

    @Bean
    public IPKeyResolver ipKeyResolver() {
        return new IPKeyResolver();
    }
}
