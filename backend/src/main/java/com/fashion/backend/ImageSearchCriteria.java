package com.fashion.backend;

import java.util.List;

public record ImageSearchCriteria(
		List<String> imageIds,
		String garmentType,
		String style,
		String material,
		String color,
		String pattern,
		String season,
		String occasion,
		String consumerProfile,
		String trend,
		String continent,
		String country,
		String city,
		String year,
		String month,
		String designer) {
}
