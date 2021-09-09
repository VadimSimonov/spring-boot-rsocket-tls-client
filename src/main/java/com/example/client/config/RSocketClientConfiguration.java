package com.example.client.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.io.File;

@Configuration
public class RSocketClientConfiguration {

    @Value("${server.url}")
    private String serverUrl;

    public RSocketStrategies rSocketStrategies;

    public RSocketClientConfiguration(RSocketStrategies rSocketStrategies) {
        this.rSocketStrategies = rSocketStrategies;
    }

    @Bean
    public RSocketRequester connectRequest() throws SSLException {
        String cert = RSocketClientConfiguration.class.getClassLoader().getResource("certs/certificate.pem").getFile();
        SslContext sslClient = SslContextBuilder.forClient()
                .trustManager(new File(cert))
                .build();

        HttpClient client = HttpClient.create();
        client
                .secure(spec -> spec.sslContext(sslClient))
                .wiretap(true)
                .baseUrl(serverUrl);

        return RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies())
                .dataMimeType(MediaType.APPLICATION_JSON)
                .metadataMimeType(MimeType.valueOf("message/x.rsocket.composite-metadata.v0"))
                .transport(WebsocketClientTransport.create(client, "/rsocket"));


    }



    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .encoders(encoders -> {
                    encoders.add(new Jackson2CborEncoder());
                    encoders.add(new Jackson2JsonEncoder());
                })
                .decoders(decoders -> {
                    decoders.add(new Jackson2CborDecoder());
                    decoders.add(new Jackson2JsonDecoder());
                })
                .build();
    }
}
