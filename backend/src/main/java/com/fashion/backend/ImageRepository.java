package com.fashion.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageRepository {

	private final JdbcTemplate jdbcTemplate;

	public ImageRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public ImageRecord save(ImageRecord image) {
		jdbcTemplate.update("""
				INSERT INTO images (
					id,
					original_filename,
					stored_filename,
					image_url,
					content_type,
					designer,
					captured_at,
					continent,
					country,
					city
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""",
				image.id(),
				image.originalFilename(),
				image.storedFilename(),
				image.imageUrl(),
				image.contentType(),
				image.designer(),
				image.capturedAt(),
				image.continent(),
				image.country(),
				image.city());
		return findById(image.id()).orElseThrow();
	}

	public List<ImageRecord> findAll() {
		return jdbcTemplate.query("""
				SELECT *
				FROM images
				ORDER BY created_at DESC
				""", this::mapImage);
	}

	public Optional<ImageRecord> findById(String id) {
		List<ImageRecord> records = jdbcTemplate.query("""
				SELECT *
				FROM images
				WHERE id = ?
				""", this::mapImage, id);
		return records.stream().findFirst();
	}

	private ImageRecord mapImage(ResultSet rs, int rowNum) throws SQLException {
		return new ImageRecord(
				rs.getString("id"),
				rs.getString("original_filename"),
				rs.getString("stored_filename"),
				rs.getString("image_url"),
				rs.getString("content_type"),
				rs.getString("designer"),
				rs.getString("captured_at"),
				rs.getString("continent"),
				rs.getString("country"),
				rs.getString("city"),
				rs.getString("created_at"),
				rs.getString("updated_at"));
	}
}
