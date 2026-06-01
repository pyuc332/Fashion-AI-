package com.fashion.backend;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorageConfig implements WebMvcConfigurer {

	private final Path uploadPath;

	public StorageConfig(@Value("${app.upload-dir}") String uploadDir) {
		this.uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String uploadLocation = uploadPath.toUri().toString();
		if (!uploadLocation.endsWith("/")) {
			uploadLocation = uploadLocation + "/";
		}
		registry
				.addResourceHandler("/uploads/**")
				.addResourceLocations(uploadLocation);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
				.addMapping("/api/**")
				.allowedOrigins("http://localhost:3000")
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("*");
	}
}
