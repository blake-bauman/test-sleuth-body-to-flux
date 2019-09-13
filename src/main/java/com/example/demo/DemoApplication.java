package com.example.demo;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@SpringBootApplication
public class DemoApplication {

    private static int port = 0;

    public static void main(final String[] args) {
        final WireMockConfiguration options = wireMockConfig().dynamicPort();
        final WireMockServer wireMockServer = new WireMockServer(options);
        wireMockServer.start();

        port = wireMockServer.port();
        configureFor(port);

        givenThat(get(urlEqualTo("/single")).willReturn(ok().withBody("{\"bar\": \"bar1\"}")
                                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON.toString())));
        givenThat(get(urlEqualTo("/multi")).willReturn(ok().withBody("[{\"bar\": \"bar1\"},{\"bar\": \"bar2\"}]")
                                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON.toString())));

        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> singleResult(final WebClient.Builder builder) {
        final WebClient client = builder.build();
        return route(GET("/mono"), request -> {
            final Mono<Foo> result = client
                                            .get()
                                            .uri("http://localhost:{port}/single", port)
                                            .retrieve()
                                            .bodyToMono(Foo.class);
            return ServerResponse.ok().contentType(APPLICATION_JSON).body(result, Foo.class);
        });
    }

    @Bean
    public RouterFunction<ServerResponse> multiResult(final WebClient.Builder builder) {
        final WebClient client = builder.build();
        return route(GET("/flux"), request -> {
            final Flux<Foo> result = client
                                            .get()
                                            .uri("http://localhost:{port}/multi", port)
                                            .retrieve()
                                            .bodyToFlux(Foo.class);
            return ServerResponse.ok().contentType(APPLICATION_JSON).body(result, Foo.class);
        });
    }

    @Bean
    public RouterFunction<ServerResponse> multiResultWithMap(final WebClient.Builder builder) {
        final WebClient client = builder.build();
        return route(GET("/flux-mapped"), request -> {
            final Flux<String> result = client
                        .get()
                        .uri("http://localhost:{port}/multi", port)
                        .retrieve()
                        .bodyToFlux(Foo.class)
                        .map(Foo::getBar);
            return ServerResponse.ok().contentType(APPLICATION_JSON).body(result, String.class);
        });
    }


    public static class Foo {
        private String bar;

        public String getBar() {
            return bar;
        }

        public void setBar(final String bar) {
            this.bar = bar;
        }
    }
}
