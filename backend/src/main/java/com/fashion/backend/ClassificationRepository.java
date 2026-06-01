package com.fashion.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ClassificationRepository {

	private final JdbcTemplate jdbcTemplate;

	public ClassificationRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public ClassificationRecord upsert(ClassificationRecord classification) {
		jdbcTemplate.update("""
				INSERT INTO classifications (
					id,
					image_id,
					description,
					garment_type_json,
					style_json,
					material_json,
					color_palette_json,
					pattern_json,
					season_json,
					occasion_json,
					consumer_profile_json,
					trend_notes_json,
					location_context_json,
					confidence_notes,
					raw_model_json,
					model_name
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				ON CONFLICT(image_id) DO UPDATE SET
					description = excluded.description,
					garment_type_json = excluded.garment_type_json,
					style_json = excluded.style_json,
					material_json = excluded.material_json,
					color_palette_json = excluded.color_palette_json,
					pattern_json = excluded.pattern_json,
					season_json = excluded.season_json,
					occasion_json = excluded.occasion_json,
					consumer_profile_json = excluded.consumer_profile_json,
					trend_notes_json = excluded.trend_notes_json,
					location_context_json = excluded.location_context_json,
					confidence_notes = excluded.confidence_notes,
					raw_model_json = excluded.raw_model_json,
					model_name = excluded.model_name,
					updated_at = CURRENT_TIMESTAMP
				""",
				classification.id(),
				classification.imageId(),
				classification.description(),
				classification.garmentTypeJson(),
				classification.styleJson(),
				classification.materialJson(),
				classification.colorPaletteJson(),
				classification.patternJson(),
				classification.seasonJson(),
				classification.occasionJson(),
				classification.consumerProfileJson(),
				classification.trendNotesJson(),
				classification.locationContextJson(),
				classification.confidenceNotes(),
				classification.rawModelJson(),
				classification.modelName());
		return findByImageId(classification.imageId()).orElseThrow();
	}

	public Optional<ClassificationRecord> findByImageId(String imageId) {
		List<ClassificationRecord> records = jdbcTemplate.query("""
				SELECT *
				FROM classifications
				WHERE image_id = ?
				""", this::mapClassification, imageId);
		return records.stream().findFirst();
	}

	public List<ClassificationRecord> findAll() {
		return jdbcTemplate.query("""
				SELECT *
				FROM classifications
				ORDER BY created_at DESC
				""", this::mapClassification);
	}

	private ClassificationRecord mapClassification(ResultSet rs, int rowNum) throws SQLException {
		return new ClassificationRecord(
				rs.getString("id"),
				rs.getString("image_id"),
				rs.getString("description"),
				rs.getString("garment_type_json"),
				rs.getString("style_json"),
				rs.getString("material_json"),
				rs.getString("color_palette_json"),
				rs.getString("pattern_json"),
				rs.getString("season_json"),
				rs.getString("occasion_json"),
				rs.getString("consumer_profile_json"),
				rs.getString("trend_notes_json"),
				rs.getString("location_context_json"),
				rs.getString("confidence_notes"),
				rs.getString("raw_model_json"),
				rs.getString("model_name"),
				rs.getString("created_at"),
				rs.getString("updated_at"));
	}
}
