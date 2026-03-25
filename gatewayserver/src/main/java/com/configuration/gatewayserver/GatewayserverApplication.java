package com.configuration.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.cloud.client.circuitbreaker.Customizer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserverApplication.class, args);
    }

    @Bean
    public RouteLocator microservicesRoutes(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(path -> path.path("/financial/accounts/**")
                        .filters(filter -> filter.rewritePath("/financial/accounts/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker((circuitBreakerConfig) -> circuitBreakerConfig.setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/contact-support")))
                        .uri("lb://ACCOUNTS"))
                .route(path -> path.path("/financial/cards/**")
                        .filters(filter -> filter.rewritePath("/financial/cards/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver()))
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2, true)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR))
                                .circuitBreaker((circuitBreakerConfig) -> circuitBreakerConfig.setName("cardsCircuitBreaker")
                                        .setFallbackUri("forward:/contact-support")))
                        .uri("lb://CARDS"))
                .route(path -> path.path("/financial/loans/**")
                        .filters(filter -> filter.rewritePath("/financial/loans/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver()))
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2, true)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR))
                                .circuitBreaker((circuitBreakerConfig) -> circuitBreakerConfig.setName("cardsCircuitBreaker")
                                        .setFallbackUri("forward:/contact-support")))
                        .uri("lb://LOANS"))
                .build();
    }

    // The above route configuration does the following:
    // 1. It defines a route for the accounts microservice that matches any path starting with /financial/accounts/. It rewrites the path to remove the /financial/accounts prefix before forwarding the
    //    request to the accounts microservice registered with Eureka under the name "ACCOUNTS".
    // 2. It defines a route for the cards microservice that matches any path starting with /financial/cards/. It rewrites the path to remove the /financial/cards prefix before forwarding the request to the cards microservice registered with Eureka under the name "CARDS".
    // 3. It defines a route for the loans microservice that matches any path starting with /financial/loans/. It rewrites the path to remove the /financial/loans prefix before forwarding the request to the loans microservice registered with Eureka under the name "LOANS".
    // 4. The "lb://" prefix in the URI indicates that the gateway should use load balancing to route requests to the appropriate instances of the microservices registered with Eureka. This allows for better scalability and fault tolerance, as the gateway can distribute requests across multiple instances of each microservice.


    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(14))
                        .build())
                .build());
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
                .defaultIfEmpty("anonymous");
    }

    // For each user, allow 1 request per second with a burst capacity of 1 and a replenish rate of 1.
    // This means that if a user makes more than 1 request in a second, the excess requests will be rejected until the rate limit is replenished.
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 10, 10); // 1 request per second with a burst capacity of 1 and a replenish rate of 1
    }
}
