package com.configuration.gatewayserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/contact-support")
    public Mono<String> accountsServiceFallback() {
        return Mono.just("Downstream service is currently unavailable. Please contact support team");
    }
}
