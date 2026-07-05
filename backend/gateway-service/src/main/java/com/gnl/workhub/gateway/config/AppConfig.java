package com.gnl.workhub.gateway.config;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        var factory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
        var rt = new RestTemplate(factory);
        rt.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
                return false;
            }
            @Override
            public void handleError(URI url, org.springframework.http.HttpMethod method, org.springframework.http.client.ClientHttpResponse response) {
                // no-op — we handle errors in the ResponseExtractor
            }
        });
        return rt;
    }
}
