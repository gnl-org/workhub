package com.gnl.workhub.gateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(GatewayApplication.class, args);
    }

    // TODO: replace with DotenvConfig.java
    private static void loadDotenv() {
        for (String dir : new String[]{".", "gateway-service", "backend/gateway-service"}) {
            if (Files.exists(Path.of(dir, ".env"))) {
                try {
                    Dotenv dotenv = Dotenv.configure().directory(dir).load();
                    dotenv.entries().forEach(entry ->
                            System.setProperty(entry.getKey(), entry.getValue())
                    );
                    return;
                } catch (Exception ignored) {}
            }
        }
    }
}
