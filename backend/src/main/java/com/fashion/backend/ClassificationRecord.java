package com.fashion.backend;

public record ClassificationRecord(
		String id,
		String imageId,
		String description,
		String garmentTypeJson,
		String styleJson,
		String materialJson,
		String colorPaletteJson,
		String patternJson,
		String seasonJson,
		String occasionJson,
		String consumerProfileJson,
		String trendNotesJson,
		String locationContextJson,
		String confidenceNotes,
		String rawModelJson,
		String modelName,
		String createdAt,
		String updatedAt) {
}
