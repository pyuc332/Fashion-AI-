package com.fashion.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnnotationRepository {

	private final JdbcTemplate jdbcTemplate;

	public AnnotationRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public AnnotationRecord upsert(AnnotationRecord annotation) {
		jdbcTemplate.update("""
				INSERT INTO annotations (
					id,
					image_id,
					tags_json,
					notes,
					observations
				) VALUES (?, ?, ?, ?, ?)
				ON CONFLICT(image_id) DO UPDATE SET
					tags_json = excluded.tags_json,
					notes = excluded.notes,
					observations = excluded.observations,
					updated_at = CURRENT_TIMESTAMP
				""",
				annotation.id(),
				annotation.imageId(),
				annotation.tagsJson(),
				annotation.notes(),
				annotation.observations());
		return findByImageId(annotation.imageId()).orElseThrow();
	}

	public Optional<AnnotationRecord> findByImageId(String imageId) {
		List<AnnotationRecord> records = jdbcTemplate.query("""
				SELECT *
				FROM annotations
				WHERE image_id = ?
				""", this::mapAnnotation, imageId);
		return records.stream().findFirst();
	}

	private AnnotationRecord mapAnnotation(ResultSet rs, int rowNum) throws SQLException {
		return new AnnotationRecord(
				rs.getString("id"),
				rs.getString("image_id"),
				rs.getString("tags_json"),
				rs.getString("notes"),
				rs.getString("observations"),
				rs.getString("created_at"),
				rs.getString("updated_at"));
	}
}
