package com.fashion.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	public List<ImageRecord> findByIds(List<String> ids) {
		return orderByIds(ids, findByCriteria(new ImageSearchCriteria(ids, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null)));
	}

	public List<ImageRecord> findByCriteria(ImageSearchCriteria criteria) {
		List<Object> params = new ArrayList<>();
		StringBuilder sql = new StringBuilder("""
				SELECT DISTINCT i.*
				FROM images i
				LEFT JOIN classifications c ON c.image_id = i.id
				WHERE 1 = 1
				""");

		if (criteria.imageIds() != null) {
			if (criteria.imageIds().isEmpty()) {
				return List.of();
			}
			appendInClause(sql, params, "i.id", criteria.imageIds());
		}
		appendJsonFilter(sql, params, "c.garment_type_json", criteria.garmentType());
		appendJsonFilter(sql, params, "c.style_json", criteria.style());
		appendJsonFilter(sql, params, "c.material_json", criteria.material());
		appendJsonFilter(sql, params, "c.color_palette_json", criteria.color());
		appendJsonFilter(sql, params, "c.pattern_json", criteria.pattern());
		appendJsonFilter(sql, params, "c.season_json", criteria.season());
		appendJsonFilter(sql, params, "c.occasion_json", criteria.occasion());
		appendJsonFilter(sql, params, "c.consumer_profile_json", criteria.consumerProfile());
		appendJsonFilter(sql, params, "c.trend_notes_json", criteria.trend());
		appendTextFilter(sql, params, "i.continent", criteria.continent());
		appendTextFilter(sql, params, "i.country", criteria.country());
		appendTextFilter(sql, params, "i.city", criteria.city());
		appendTextFilter(sql, params, "i.designer", criteria.designer());
		if (criteria.year() != null && !criteria.year().isBlank()) {
			sql.append(" AND i.captured_at LIKE ?");
			params.add(criteria.year().trim() + "-%");
		}
		if (criteria.month() != null && !criteria.month().isBlank()) {
			sql.append(" AND substr(i.captured_at, 6, 2) = ?");
			params.add(criteria.month().trim().length() == 1 ? "0" + criteria.month().trim() : criteria.month().trim());
		}
		sql.append(" ORDER BY i.created_at DESC");

		List<ImageRecord> records = jdbcTemplate.query(sql.toString(), this::mapImage, params.toArray());
		if (criteria.imageIds() != null) {
			return orderByIds(criteria.imageIds(), records);
		}
		return records;
	}

	private void appendInClause(StringBuilder sql, List<Object> params, String column, List<String> values) {
		sql.append(" AND ").append(column).append(" IN (");
		sql.append(String.join(",", values.stream().map(value -> "?").toList()));
		sql.append(")");
		params.addAll(values);
	}

	private void appendTextFilter(StringBuilder sql, List<Object> params, String column, String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		sql.append(" AND lower(").append(column).append(") = lower(?)");
		params.add(value.trim());
	}

	private void appendJsonFilter(StringBuilder sql, List<Object> params, String column, String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		sql.append(" AND ").append(column).append(" LIKE ?");
		params.add("%\"" + value.trim().toLowerCase() + "\"%");
	}

	private List<ImageRecord> orderByIds(List<String> ids, List<ImageRecord> records) {
		Map<String, ImageRecord> byId = new LinkedHashMap<>();
		for (ImageRecord record : records) {
			byId.put(record.id(), record);
		}
		List<ImageRecord> ordered = new ArrayList<>();
		for (String id : ids) {
			ImageRecord record = byId.get(id);
			if (record != null) {
				ordered.add(record);
			}
		}
		return ordered;
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
