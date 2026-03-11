package com.configuration.gatewayserver.filters;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class RequestTraceFilter implements GlobalFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestTraceFilter.class);

    @Autowired
    private FilterUtility filterUtility;


    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        if(isCorrelationIdPresent(headers)){
            LOGGER.debug("Correlation id found in tracing filter: {}. ", filterUtility.getCorrelationId(headers));
        } else {
            String correlationId = filterUtility.generateCorrelationId();
            exchange = filterUtility.setCorrelationId(exchange, correlationId);
            LOGGER.debug("Generated new correlation in RequestTraceFilter id: {}. ", correlationId);
        }
        return chain.filter(exchange);
    }

    private  boolean isCorrelationIdPresent(final HttpHeaders requestHeaders) {
        return filterUtility.getCorrelationId(requestHeaders) != null;
    }


}
