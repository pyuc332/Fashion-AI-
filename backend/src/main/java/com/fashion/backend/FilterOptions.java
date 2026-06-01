package com.fashion.backend;

import java.util.List;

public record FilterOptions(
		List<String> garmentTypes,
		List<String> styles,
		List<String> materials,
		List<String> colors,
		List<String> patterns,
		List<String> seasons,
		List<String> occasions,
		List<String> consumerProfiles,
		List<String> trends,
		List<String> continents,
		List<String> countries,
		List<String> cities,
		List<String> years,
		List<String> months,
		List<String> designers) {
}
