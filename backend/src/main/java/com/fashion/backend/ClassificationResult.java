package com.fashion.backend;

import java.util.List;
import java.util.Map;

public record ClassificationResult(
		String description,
		List<String> garmentType,
		List<String> style,
		List<String> material,
		List<String> colorPalette,
		List<String> pattern,
		List<String> season,
		List<String> occasion,
		List<String> consumerProfile,
		List<String> trendNotes,
		Map<String, String> locationContext,
		String confidenceNotes) {
}
