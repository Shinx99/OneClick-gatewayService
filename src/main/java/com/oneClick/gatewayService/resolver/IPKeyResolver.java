// src/main/java/com/oneclick/gatewayService/resolver/IPKeyResolver.java
package com.oneClick.gatewayService.resolver;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("ipKeyResolver")
public class IPKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return Mono.just("unknown");
        }
        return Mono.just(remoteAddress.getAddress().getHostAddress());
    }
}
