
package com.example.client.controller;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
public class RsocketClientController {

    private final RSocketRequester connectRequest;

    public RsocketClientController(RSocketRequester connectRequest) {
        this.connectRequest = connectRequest;
    }


    @GetMapping("/request-response-hello")
    public Mono<String> getHello() {
        return connectRequest
                .route("request-response-hello")
                .retrieveMono(String.class);
    }

}
