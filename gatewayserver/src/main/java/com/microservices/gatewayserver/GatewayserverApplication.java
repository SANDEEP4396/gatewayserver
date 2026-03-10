package com.microservices.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayserverApplication.class, args);
	}

	@Bean
	public RouteLocator microservicesRoutes(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(path -> path.path("/financial/accounts/**")
						.filters(filter -> filter.rewritePath("/financial/accounts/(?<segment>.*)", "/${segment}"))
						.uri("lb://ACCOUNTS"))
				.route(path -> path.path("/financial/cards/**")
						.filters(filter -> filter.rewritePath("/financial//cards/(?<segment>.*)", "/${segment}"))
						.uri("lb://CARDS"))
				.route(path -> path.path("/financial/loans/**")
						.filters(filter -> filter.rewritePath("/financial//loans/(?<segment>.*)", "/${segment}"))
						.uri("lb://LOANS"))
				.build();
	}

	// The above route configuration does the following:
	// 1. It defines a route for the accounts microservice that matches any path starting with /financial/accounts/. It rewrites the path to remove the /financial/accounts prefix before forwarding the
	//    request to the accounts microservice registered with Eureka under the name "ACCOUNTS".
	// 2. It defines a route for the cards microservice that matches any path starting with /financial/cards/. It rewrites the path to remove the /financial/cards prefix before forwarding the request to the cards microservice registered with Eureka under the name "CARDS".
	// 3. It defines a route for the loans microservice that matches any path starting with /financial/loans/. It rewrites the path to remove the /financial/loans prefix before forwarding the request to the loans microservice registered with Eureka under the name "LOANS".
	// 4. The "lb://" prefix in the URI indicates that the gateway should use load balancing to route requests to the appropriate instances of the microservices registered with Eureka. This allows for better scalability and fault tolerance, as the gateway can distribute requests across multiple instances of each microservice.
}
