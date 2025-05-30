package com.Oracle.FileService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.Oracle.FileService")
public class FileServiceApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // Optional: avoids crash if .env is missing
				.load();
		System.setProperty("JWT_SECRET_ORACLE", dotenv.get("JWT_SECRET_ORACLE"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		SpringApplication.run(FileServiceApplication.class, args);
	}
}
