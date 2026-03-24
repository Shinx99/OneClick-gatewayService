// src/main/java/com/oneclick/gatewayService/IPKeyResolver.java
package com.oneClick.gatewayService.resolver;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class IPKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getRemoteAddress()
            .getAddress().getHostAddress());
    }
}
