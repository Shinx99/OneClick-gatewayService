package com.oneClick.gatewayService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
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
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                
                // Add CORS headers for WebSocket
                headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
                headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                headers.add("Access-Control-Max-Age", "3600");
                headers.add("Access-Control-Allow-Headers", "authorization, content-type, Authorization, Content-Type, access_token");
                headers.add("Access-Control-Allow-Credentials", "true");
                
                // Handle preflight
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
                
                // Log WebSocket connection attempt
                log.info("🔌 WebSocket connection to: {}", path);
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null) {
                    log.info("🔑 Authorization header present: Bearer token");
                } else {
                    // Check query param for token
                    String token = request.getQueryParams().getFirst("access_token");
                    if (token != null) {
                        log.info("🔑 Token found in query param");
                        // Add token to Authorization header for downstream
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.filter(ctx.mutate().request(mutatedRequest).build());
                    }
                    log.warn("⚠️ No authorization token found for WebSocket connection");
                }
            }
            
            return chain.filter(ctx);
        };
    }
}