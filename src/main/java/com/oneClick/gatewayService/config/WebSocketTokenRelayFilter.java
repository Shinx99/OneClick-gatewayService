package com.oneClick.gatewayService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@Slf4j
public class WebSocketTokenRelayFilter extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            
            if (path.startsWith("/ws")) {
                // Get token from query param
                String token = request.getQueryParams().getFirst("access_token");
                
                if (token != null) {
                    log.debug("Relaying token from query param to Authorization header for WebSocket");
                    // Add token to Authorization header
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }
                
                // Check existing Authorization header
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    log.debug("Authorization header already present for WebSocket");
                } else {
                    log.warn("No token found for WebSocket connection to: {}", path);
                }
            }
            
            return chain.filter(exchange);
        };
    }
}