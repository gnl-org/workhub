package com.gnl.workhub.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to load environment variables from .env file.
 * Loads from project root directory. Works in all environments:
 * - Local dev: reads from root .env
 * - Docker Compose: env vars already set by compose, dotenv skips if .env missing
 * - Production (Kubernetes): env vars injected, dotenv skips if .env missing
 */
@Configuration
public class DotenvConfig {

    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
