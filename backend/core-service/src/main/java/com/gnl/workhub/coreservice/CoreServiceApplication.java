package com.gnl.workhub.coreservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class CoreServiceApplication {

	public static void main(String[] args) {
		loadDotenv();
		SpringApplication.run(CoreServiceApplication.class, args);
	}

	// TODO: replace with DotenvConfig.java (@Configuration + EnvironmentPostProcessor)
	// to avoid loading env vars in main(). The challenge is ensuring properties are
	// available early enough for @Value("${...}") and datasource config.
	private static void loadDotenv() {
		for (String dir : new String[]{".", "core-service", "backend/core-service"}) {
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
