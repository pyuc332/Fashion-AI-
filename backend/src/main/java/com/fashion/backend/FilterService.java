package com.fashion.backend;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FilterService {

	private final ImageRepository imageRepository;
	private final ClassificationRepository classificationRepository;
	private final ObjectMapper objectMapper;

	public FilterService(
			ImageRepository imageRepository,
			ClassificationRepository classificationRepository,
			ObjectMapper objectMapper) {
		this.imageRepository = imageRepository;
		this.classificationRepository = classificationRepository;
		this.objectMapper = objectMapper;
	}

	public FilterOptions getFilterOptions() {
		Set<String> garmentTypes = new TreeSet<>();
		Set<String> styles = new TreeSet<>();
		Set<String> materials = new TreeSet<>();
		Set<String> colors = new TreeSet<>();
		Set<String> patterns = new TreeSet<>();
		Set<String> seasons = new TreeSet<>();
		Set<String> occasions = new TreeSet<>();
		Set<String> consumerProfiles = new TreeSet<>();
		Set<String> trends = new TreeSet<>();
		Set<String> continents = new TreeSet<>();
		Set<String> countries = new TreeSet<>();
		Set<String> cities = new TreeSet<>();
		Set<String> years = new TreeSet<>();
		Set<String> months = new TreeSet<>();
		Set<String> designers = new TreeSet<>();

		for (ImageRecord image : imageRepository.findAll()) {
			addIfPresent(continents, image.continent());
			addIfPresent(countries, image.country());
			addIfPresent(cities, image.city());
			addIfPresent(designers, image.designer());
			if (image.capturedAt() != null && image.capturedAt().length() >= 7) {
				addIfPresent(years, image.capturedAt().substring(0, 4));
				addIfPresent(months, image.capturedAt().substring(5, 7));
			}
		}

		for (ClassificationRecord classification : classificationRepository.findAll()) {
			garmentTypes.addAll(readList(classification.garmentTypeJson()));
			styles.addAll(readList(classification.styleJson()));
			materials.addAll(readList(classification.materialJson()));
			colors.addAll(readList(classification.colorPaletteJson()));
			patterns.addAll(readList(classification.patternJson()));
			seasons.addAll(readList(classification.seasonJson()));
			occasions.addAll(readList(classification.occasionJson()));
			consumerProfiles.addAll(readList(classification.consumerProfileJson()));
			trends.addAll(readList(classification.trendNotesJson()));
		}

		return new FilterOptions(
				List.copyOf(garmentTypes),
				List.copyOf(styles),
				List.copyOf(materials),
				List.copyOf(colors),
				List.copyOf(patterns),
				List.copyOf(seasons),
				List.copyOf(occasions),
				List.copyOf(consumerProfiles),
				List.copyOf(trends),
				List.copyOf(continents),
				List.copyOf(countries),
				List.copyOf(cities),
				List.copyOf(years),
				List.copyOf(months),
				List.copyOf(designers));
	}

	private List<String> readList(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(json, new TypeReference<List<String>>() {
			}).stream()
					.filter(value -> value != null && !value.isBlank())
					.toList();
		} catch (IOException ex) {
			return List.of();
		}
	}

	private void addIfPresent(Set<String> values, String value) {
		if (value != null && !value.isBlank()) {
			values.add(value.trim());
		}
	}
}
