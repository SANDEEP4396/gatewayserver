package com.configuration.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
public class ResponseTraceFilter  {

    private  static final Logger LOGGER = LoggerFactory.getLogger(ResponseTraceFilter.class);

    @Autowired
    private FilterUtility filterUtility;

   @Bean
    public GlobalFilter filter() {
       return ((exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
           HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
           String correlationId = filterUtility.getCorrelationId(requestHeaders);
           LOGGER.debug("Completing outgoing request for {} with correlation id: {}. ", exchange.getRequest().getURI(), correlationId);
           exchange.getResponse().getHeaders().add(FilterUtility.CORRELATION_ID, correlationId);
       })));
   }

   // responseTraceFilter() method is defined as a bean to ensure that it is registered as a global filter in the Spring context.
    // .then(Mono.fromRunnable(() -> { ... })) is used to execute the code inside the lambda expression after the response has been processed by the downstream filters and handlers.
    // This allows us to add the correlation ID to the response headers before the response is sent back to the client.
    // Mono.fromRunnable() is a Reactor utility that creates a Mono that executes the provided Runnable when subscribed to.
    // In this case, it ensures that the code inside the lambda expression is executed after the response processing is complete.
}
