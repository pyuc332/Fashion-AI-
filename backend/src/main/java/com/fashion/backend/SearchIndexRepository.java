package com.fashion.backend;

import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SearchIndexRepository {

	private final JdbcTemplate jdbcTemplate;

	public SearchIndexRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void updateClassification(ClassificationRecord classification) {
		String manualAnnotationText = jdbcTemplate.query("""
				SELECT tags_json || ' ' || coalesce(notes, '') || ' ' || coalesce(observations, '')
				FROM annotations
				WHERE image_id = ?
				""", rs -> rs.next() ? rs.getString(1) : "", classification.imageId());
		refreshImage(classification.imageId(), classification, manualAnnotationText);
	}

	public void refreshImage(String imageId, ClassificationRecord classification, AnnotationRecord annotation) {
		refreshImage(imageId, classification, flattenAnnotation(annotation));
	}

	private void refreshImage(String imageId, ClassificationRecord classification, String manualAnnotationText) {
		jdbcTemplate.update("DELETE FROM image_search_fts WHERE image_id = ?", imageId);
		jdbcTemplate.update("""
				INSERT INTO image_search_fts (
					image_id,
					description,
					ai_metadata_text,
					manual_annotation_text
				) VALUES (?, ?, ?, ?)
				""",
				imageId,
				classification == null ? "" : classification.description(),
				classification == null ? "" : flattenMetadata(classification),
				manualAnnotationText == null ? "" : manualAnnotationText);
	}

	public List<String> searchImageIds(String query) {
		String normalizedQuery = normalizeQuery(query);
		if (normalizedQuery.isBlank()) {
			return List.of();
		}
		return jdbcTemplate.queryForList("""
				SELECT image_id
				FROM image_search_fts
				WHERE image_search_fts MATCH ?
				ORDER BY bm25(image_search_fts)
				""", String.class, normalizedQuery);
	}

	private String flattenMetadata(ClassificationRecord classification) {
		return String.join(" ",
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
				nullToBlank(classification.confidenceNotes()));
	}

	private String flattenAnnotation(AnnotationRecord annotation) {
		if (annotation == null) {
			return "";
		}
		return String.join(" ",
				annotation.tagsJson(),
				nullToBlank(annotation.notes()),
				nullToBlank(annotation.observations()));
	}

	private String normalizeQuery(String query) {
		if (query == null) {
			return "";
		}
		return query.toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ")
				.trim();
	}

	private String nullToBlank(String value) {
		return value == null ? "" : value;
	}
}
