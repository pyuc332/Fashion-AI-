package com.fashion.backend;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ClassificationService {

	public static final String MOCK_MODEL_NAME = "mock-fashion-vision-v1";

	private final ClassificationParser parser;
	private final ClassificationRepository classificationRepository;
	private final SearchIndexRepository searchIndexRepository;

	public ClassificationService(
			ClassificationParser parser,
			ClassificationRepository classificationRepository,
			SearchIndexRepository searchIndexRepository) {
		this.parser = parser;
		this.classificationRepository = classificationRepository;
		this.searchIndexRepository = searchIndexRepository;
	}

	public ClassificationRecord saveMockClassification(String imageId) {
		return saveClassification(imageId, mockClassificationJson(), MOCK_MODEL_NAME);
	}

	public ClassificationRecord saveClassification(String imageId, String rawJson, String modelName) {
		ClassificationResult result = parser.parse(rawJson);
		ClassificationRecord record = new ClassificationRecord(
				UUID.randomUUID().toString(),
				imageId,
				result.description(),
				parser.toJson(result.garmentType()),
				parser.toJson(result.style()),
				parser.toJson(result.material()),
				parser.toJson(result.colorPalette()),
				parser.toJson(result.pattern()),
				parser.toJson(result.season()),
				parser.toJson(result.occasion()),
				parser.toJson(result.consumerProfile()),
				parser.toJson(result.trendNotes()),
				parser.toJson(result.locationContext()),
				result.confidenceNotes(),
				rawJson,
				modelName,
				null,
				null);
		ClassificationRecord saved = classificationRepository.upsert(record);
		searchIndexRepository.updateClassification(saved);
		return saved;
	}

	private String mockClassificationJson() {
		return """
				{
				  "description": "A cream linen summer dress with an embroidered indigo neckline, relaxed resort styling, and artisan market influence.",
				  "garment_type": ["Dress"],
				  "style": ["Resort", "Bohemian"],
				  "material": ["Linen"],
				  "color_palette": ["Cream", "Indigo"],
				  "pattern": ["Embroidered"],
				  "season": ["Summer"],
				  "occasion": ["Casual", "Market"],
				  "consumer_profile": ["Fashion-conscious traveler"],
				  "trend_notes": ["Artisan detailing", "Natural fibers"],
				  "location_context": {
				    "continent": "North America",
				    "country": "United States",
				    "city": "Los Angeles"
				  },
				  "confidence_notes": "Material and consumer profile are inferred visually in this mock response."
				}
				""";
	}
}
