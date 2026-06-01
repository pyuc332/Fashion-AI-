package com.fashion.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ClassificationParserTests {

	private final ClassificationParser parser = new ClassificationParser(new ObjectMapper());

	@Test
	void parsesAndNormalizesValidModelJson() {
		ClassificationResult result = parser.parse("""
				{
				  "description": "A cream linen summer dress with an embroidered neckline.",
				  "garment_type": ["Dress", "dress", " "],
				  "style": ["Resort"],
				  "material": ["Linen"],
				  "color_palette": ["Cream", "Indigo"],
				  "pattern": ["Embroidered"],
				  "season": ["Summer"],
				  "occasion": ["Market"],
				  "consumer_profile": ["Fashion-conscious traveler"],
				  "trend_notes": ["Artisan detailing"],
				  "location_context": {
				    "continent": "North America",
				    "country": "United States",
				    "city": "Los Angeles"
				  },
				  "confidence_notes": "Material is inferred."
				}
				""");

		assertThat(result.description()).contains("embroidered neckline");
		assertThat(result.garmentType()).containsExactly("dress");
		assertThat(result.colorPalette()).containsExactly("cream", "indigo");
		assertThat(result.locationContext())
				.containsEntry("country", "United States")
				.containsEntry("city", "Los Angeles");
		assertThat(result.confidenceNotes()).isEqualTo("Material is inferred.");
	}

	@Test
	void allowsMissingOptionalArrayFields() {
		ClassificationResult result = parser.parse("""
				{
				  "description": "A black jacket.",
				  "garment_type": ["Jacket"]
				}
				""");

		assertThat(result.garmentType()).containsExactly("jacket");
		assertThat(result.material()).isEmpty();
		assertThat(result.locationContext()).isEmpty();
	}

	@Test
	void rejectsMissingDescription() {
		assertThatThrownBy(() -> parser.parse("""
				{
				  "garment_type": ["Dress"]
				}
				"""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("description");
	}

	@Test
	void rejectsMissingGarmentType() {
		assertThatThrownBy(() -> parser.parse("""
				{
				  "description": "A garment."
				}
				"""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("garment_type");
	}

	@Test
	void rejectsInvalidJson() {
		assertThatThrownBy(() -> parser.parse("{not-json"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("valid JSON");
	}
}
