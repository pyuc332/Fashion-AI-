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
		jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS classifications (
					id TEXT PRIMARY KEY,
					image_id TEXT NOT NULL UNIQUE,
					description TEXT NOT NULL,
					garment_type_json TEXT NOT NULL,
					style_json TEXT NOT NULL,
					material_json TEXT NOT NULL,
					color_palette_json TEXT NOT NULL,
					pattern_json TEXT NOT NULL,
					season_json TEXT NOT NULL,
					occasion_json TEXT NOT NULL,
					consumer_profile_json TEXT NOT NULL,
					trend_notes_json TEXT NOT NULL,
					location_context_json TEXT NOT NULL,
					confidence_notes TEXT,
					raw_model_json TEXT NOT NULL,
					model_name TEXT NOT NULL,
					created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
					updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
					FOREIGN KEY (image_id) REFERENCES images(id)
				)
				""");
		jdbcTemplate.execute("""
				CREATE VIRTUAL TABLE IF NOT EXISTS image_search_fts USING fts5(
					image_id UNINDEXED,
					description,
					ai_metadata_text,
					manual_annotation_text
				)
				""");
	}
}
