package com.gnl.workhub.authservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    // TODO: replace with DotenvConfig.java (@Configuration + EnvironmentPostProcessor)
    private static void loadDotenv() {
        for (String dir : new String[]{".", "auth-service", "backend/auth-service"}) {
            if (Files.exists(Path.of(dir, ".env"))) {
                try {
                    Dotenv dotenv = Dotenv.configure()
                            .directory(dir)
                            .load();
                    dotenv.entries().forEach(entry ->
                            System.setProperty(entry.getKey(), entry.getValue())
                    );
                    return;
                } catch (Exception ignored) {
                }
            }
        }
    }
}
