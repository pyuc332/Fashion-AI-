package com.fashion.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OpenAiVisionClient {

	private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String apiKey;
	private final String model;

	public OpenAiVisionClient(
			RestClient.Builder restClientBuilder,
			ObjectMapper objectMapper,
			@Value("${openai.api-key}") String apiKey,
			@Value("${openai.model}") String model) {
		this.restClient = restClientBuilder.build();
		this.objectMapper = objectMapper;
		this.apiKey = apiKey;
		this.model = model;
	}

	public String classify(Path imagePath, String contentType) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new ResponseStatusException(HttpStatusCode.valueOf(503), "OPENAI_API_KEY is not configured.");
		}
		try {
			String response = restClient.post()
					.uri(RESPONSES_URL)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody(imagePath, contentType))
					.retrieve()
					.body(String.class);
			return extractOutputText(response);
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (RestClientResponseException ex) {
			throw new ResponseStatusException(
					HttpStatusCode.valueOf(502),
					"OpenAI classification request failed: " + summarizeOpenAiError(ex.getResponseBodyAsString()),
					ex);
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatusCode.valueOf(502), "OpenAI classification request failed.", ex);
		}
	}

	public String model() {
		return model;
	}

	private Map<String, Object> requestBody(Path imagePath, String contentType) throws IOException {
		String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
		String mimeType = contentType == null || contentType.isBlank() ? "image/jpeg" : contentType;
		return Map.of(
				"model", model,
				"input", List.of(Map.of(
						"role", "user",
						"content", List.of(
								Map.of(
										"type", "input_text",
										"text", classificationPrompt()),
								Map.of(
										"type", "input_image",
										"image_url", "data:" + mimeType + ";base64," + base64Image)))),
				"text", Map.of(
						"format", Map.of(
								"type", "json_schema",
								"name", "fashion_garment_classification",
								"strict", true,
								"schema", classificationSchema())));
	}

	private String classificationPrompt() {
		return """
				Classify this fashion garment image for an inspiration library.
				Return only the structured JSON required by the schema.
				Use concise, searchable terms. If a value is unknown, use an empty array or an empty string.
				Infer visually, but keep uncertainty in confidence_notes instead of overclaiming.
				""";
	}

	private Map<String, Object> classificationSchema() {
		return Map.of(
				"type", "object",
				"additionalProperties", false,
				"required", List.of(
						"description",
						"garment_type",
						"style",
						"material",
						"color_palette",
						"pattern",
						"season",
						"occasion",
						"consumer_profile",
						"trend_notes",
						"location_context",
						"confidence_notes"),
				"properties", Map.ofEntries(
						Map.entry("description", Map.of("type", "string")),
						Map.entry("garment_type", stringArraySchema()),
						Map.entry("style", stringArraySchema()),
						Map.entry("material", stringArraySchema()),
						Map.entry("color_palette", stringArraySchema()),
						Map.entry("pattern", stringArraySchema()),
						Map.entry("season", stringArraySchema()),
						Map.entry("occasion", stringArraySchema()),
						Map.entry("consumer_profile", stringArraySchema()),
						Map.entry("trend_notes", stringArraySchema()),
						Map.entry("location_context", Map.of(
								"type", "object",
								"additionalProperties", false,
								"required", List.of("continent", "country", "city"),
								"properties", Map.of(
										"continent", Map.of("type", "string"),
										"country", Map.of("type", "string"),
										"city", Map.of("type", "string")))),
						Map.entry("confidence_notes", Map.of("type", "string"))));
	}

	private Map<String, Object> stringArraySchema() {
		return Map.of(
				"type", "array",
				"items", Map.of("type", "string"));
	}

	private String extractOutputText(String responseBody) throws IOException {
		JsonNode root = objectMapper.readTree(responseBody);
		JsonNode outputText = root.path("output_text");
		if (outputText.isTextual() && !outputText.asText().isBlank()) {
			return outputText.asText();
		}

		for (JsonNode outputItem : root.path("output")) {
			for (JsonNode contentItem : outputItem.path("content")) {
				JsonNode text = contentItem.path("text");
				if (text.isTextual() && !text.asText().isBlank()) {
					return text.asText();
				}
				JsonNode refusal = contentItem.path("refusal");
				if (refusal.isTextual() && !refusal.asText().isBlank()) {
					throw new ResponseStatusException(HttpStatusCode.valueOf(502), "OpenAI refused classification: " + refusal.asText());
				}
			}
		}
		throw new ResponseStatusException(HttpStatusCode.valueOf(502), "OpenAI response did not include classification JSON.");
	}

	private String summarizeOpenAiError(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return "empty error body";
		}
		try {
			JsonNode message = objectMapper.readTree(responseBody).path("error").path("message");
			if (message.isTextual() && !message.asText().isBlank()) {
				return message.asText();
			}
		} catch (IOException ignored) {
			// Fall through to a short raw body preview.
		}
		return responseBody.length() > 300 ? responseBody.substring(0, 300) : responseBody;
	}
}
