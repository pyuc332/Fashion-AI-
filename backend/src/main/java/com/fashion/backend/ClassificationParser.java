package com.fashion.backend;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ClassificationParser {

	private final ObjectMapper objectMapper;

	public ClassificationParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ClassificationResult parse(String rawJson) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			String description = requiredText(root, "description");
			return new ClassificationResult(
					description,
					requiredStringList(root, "garment_type"),
					optionalStringList(root, "style"),
					optionalStringList(root, "material"),
					optionalStringList(root, "color_palette"),
					optionalStringList(root, "pattern"),
					optionalStringList(root, "season"),
					optionalStringList(root, "occasion"),
					optionalStringList(root, "consumer_profile"),
					optionalStringList(root, "trend_notes"),
					locationContext(root.path("location_context")),
					optionalText(root, "confidence_notes"));
		} catch (IOException ex) {
			throw new IllegalArgumentException("Classification response must be valid JSON.", ex);
		}
	}

	public String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Could not serialize classification value.", ex);
		}
	}

	private String requiredText(JsonNode root, String fieldName) {
		String value = optionalText(root, fieldName);
		if (value == null) {
			throw new IllegalArgumentException("Missing required classification field: " + fieldName);
		}
		return value;
	}

	private String optionalText(JsonNode root, String fieldName) {
		JsonNode value = root.path(fieldName);
		if (!value.isTextual() || value.asText().isBlank()) {
			return null;
		}
		return value.asText().trim();
	}

	private List<String> requiredStringList(JsonNode root, String fieldName) {
		List<String> values = optionalStringList(root, fieldName);
		if (values.isEmpty()) {
			throw new IllegalArgumentException("Missing required classification field: " + fieldName);
		}
		return values;
	}

	private List<String> optionalStringList(JsonNode root, String fieldName) {
		JsonNode value = root.path(fieldName);
		if (!value.isArray()) {
			return List.of();
		}
		return objectMapper.convertValue(value, new TypeReference<List<String>>() {
		}).stream()
				.filter(item -> item != null && !item.isBlank())
				.map(item -> item.trim().toLowerCase(Locale.ROOT))
				.distinct()
				.toList();
	}

	private Map<String, String> locationContext(JsonNode value) {
		if (!value.isObject()) {
			return Map.of();
		}
		Map<String, String> context = new LinkedHashMap<>();
		copyLocationField(value, context, "continent");
		copyLocationField(value, context, "country");
		copyLocationField(value, context, "city");
		return context;
	}

	private void copyLocationField(JsonNode source, Map<String, String> target, String fieldName) {
		JsonNode value = source.path(fieldName);
		if (value.isTextual() && !value.asText().isBlank()) {
			target.put(fieldName, value.asText().trim());
		}
	}
}
