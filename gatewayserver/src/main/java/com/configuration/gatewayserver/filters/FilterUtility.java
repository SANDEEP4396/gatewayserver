package com.configuration.gatewayserver.filters;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class FilterUtility {

    public static final String CORRELATION_ID = "X-Correlation-Id";

    public String getCorrelationId(final HttpHeaders requestHeaders) {
       return requestHeaders.getFirst(CORRELATION_ID) != null ? requestHeaders.getFirst(CORRELATION_ID) : null;
    }

    public String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String headerName, String headerValue) {
        return exchange.mutate().request(
                exchange.getRequest().mutate().header(headerName, headerValue).build()
        ).build();
    }
    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }
}
