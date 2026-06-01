package com.fashion.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.fasterxml.jackson.databind.ObjectMapper;

class FilterServiceTests {

	private Path databasePath;
	private ImageRepository imageRepository;
	private ClassificationRepository classificationRepository;
	private FilterService filterService;

	@BeforeEach
	void setUp() throws Exception {
		databasePath = Files.createTempFile("fashion-filter-test", ".db");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.sqlite.JDBC");
		dataSource.setUrl("jdbc:sqlite:" + databasePath);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		new DatabaseInitializer(jdbcTemplate).run();
		imageRepository = new ImageRepository(jdbcTemplate);
		classificationRepository = new ClassificationRepository(jdbcTemplate);
		filterService = new FilterService(imageRepository, classificationRepository, new ObjectMapper());
	}

	@AfterEach
	void tearDown() throws IOException {
		Files.deleteIfExists(databasePath);
	}

	@Test
	void generatesDeduplicatedFiltersFromImagesAndClassifications() {
		saveImage("image-1", "Designer A", "2026-06-01", "North America", "United States", "Los Angeles");
		saveImage("image-2", "Designer A", "2026-05-15", "Europe", "France", "Paris");
		saveClassification("class-1", "image-1", "[\"dress\"]", "[\"resort\"]", "[\"linen\"]", "[\"cream\"]");
		saveClassification("class-2", "image-2", "[\"dress\"]", "[\"tailored\"]", "[\"wool\"]", "[\"black\"]");

		FilterOptions filters = filterService.getFilterOptions();

		assertThat(filters.garmentTypes()).containsExactly("dress");
		assertThat(filters.styles()).containsExactly("resort", "tailored");
		assertThat(filters.materials()).containsExactly("linen", "wool");
		assertThat(filters.colors()).containsExactly("black", "cream");
		assertThat(filters.countries()).containsExactly("France", "United States");
		assertThat(filters.designers()).containsExactly("Designer A");
		assertThat(filters.years()).containsExactly("2026");
		assertThat(filters.months()).containsExactly("05", "06");
	}

	@Test
	void filtersImagesByMetadataAndContext() {
		saveImage("image-1", "Designer A", "2026-06-01", "North America", "United States", "Los Angeles");
		saveImage("image-2", "Designer B", "2026-05-15", "Europe", "France", "Paris");
		saveClassification("class-1", "image-1", "[\"dress\"]", "[\"resort\"]", "[\"linen\"]", "[\"cream\"]");
		saveClassification("class-2", "image-2", "[\"jacket\"]", "[\"tailored\"]", "[\"wool\"]", "[\"black\"]");

		assertThat(imageRepository.findByCriteria(new ImageSearchCriteria(
				null, "dress", null, null, "cream", null, null, null, null, null,
				null, "United States", null, null, null, null)))
				.extracting(ImageRecord::id)
				.containsExactly("image-1");
		assertThat(imageRepository.findByCriteria(new ImageSearchCriteria(
				null, null, null, "wool", null, null, null, null, null, null,
				null, null, "Paris", "2026", "05", "Designer B")))
				.extracting(ImageRecord::id)
				.containsExactly("image-2");
	}

	private void saveImage(
			String id,
			String designer,
			String capturedAt,
			String continent,
			String country,
			String city) {
		imageRepository.save(new ImageRecord(
				id,
				id + ".png",
				id + ".png",
				"/uploads/" + id + ".png",
				"image/png",
				designer,
				capturedAt,
				continent,
				country,
				city,
				null,
				null));
	}

	private void saveClassification(
			String id,
			String imageId,
			String garmentTypeJson,
			String styleJson,
			String materialJson,
			String colorPaletteJson) {
		classificationRepository.upsert(new ClassificationRecord(
				id,
				imageId,
				"Test description",
				garmentTypeJson,
				styleJson,
				materialJson,
				colorPaletteJson,
				"[]",
				"[]",
				"[]",
				"[]",
				"[]",
				"{}",
				null,
				"{}",
				"test-model",
				null,
				null));
	}
}
