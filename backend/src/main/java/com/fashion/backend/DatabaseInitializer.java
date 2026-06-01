package com.fashion.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

	private final JdbcTemplate jdbcTemplate;

	public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(String... args) {
		jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS images (
					id TEXT PRIMARY KEY,
					original_filename TEXT NOT NULL,
					stored_filename TEXT NOT NULL,
					image_url TEXT NOT NULL,
					content_type TEXT,
					designer TEXT,
					captured_at TEXT,
					continent TEXT,
					country TEXT,
					city TEXT,
					created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
					updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
				)
				""");
	}
}
