package com.oneClick.gatewayService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebSocketGatewayConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter websocketCorsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            String path = request.getURI().getPath();

            // Handle WebSocket upgrade requests
            if (path.startsWith("/ws")) {
                // Handle preflight - để SecurityConfig xử lý CORS
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    ServerHttpResponse response = ctx.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }

                // Chỉ log, KHÔNG thêm CORS headers (để SecurityConfig lo)
                log.info("🔌 WebSocket connection to: {}", path);
            }

            return chain.filter(ctx);
        };
    }
}