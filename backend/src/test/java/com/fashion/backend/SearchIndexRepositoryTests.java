package com.fashion.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SearchIndexRepositoryTests {

	private Path databasePath;
	private SearchIndexRepository repository;

	@BeforeEach
	void setUp() throws IOException {
		databasePath = Files.createTempFile("fashion-search-test", ".db");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.sqlite.JDBC");
		dataSource.setUrl("jdbc:sqlite:" + databasePath);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		new DatabaseInitializer(jdbcTemplate).run();
		repository = new SearchIndexRepository(jdbcTemplate);
	}

	@AfterEach
	void tearDown() throws IOException {
		Files.deleteIfExists(databasePath);
	}

	@Test
	void searchesDescriptionAndAiMetadata() {
		repository.updateClassification(classification(
				"image-1",
				"A cream linen dress with an embroidered neckline.",
				"[\"dress\"]",
				"[\"cream\",\"indigo\"]",
				"[\"artisan detailing\"]"));
		repository.updateClassification(classification(
				"image-2",
				"A black leather moto jacket.",
				"[\"jacket\"]",
				"[\"black\"]",
				"[\"streetwear\"]"));

		assertThat(repository.searchImageIds("embroidered neckline")).containsExactly("image-1");
		assertThat(repository.searchImageIds("streetwear")).containsExactly("image-2");
		assertThat(repository.searchImageIds("nonexistent")).isEmpty();
	}

	@Test
	void replacesExistingIndexForImage() {
		repository.updateClassification(classification(
				"image-1",
				"An embroidered linen dress.",
				"[\"dress\"]",
				"[\"cream\"]",
				"[\"artisan\"]"));
		repository.updateClassification(classification(
				"image-1",
				"A black leather jacket.",
				"[\"jacket\"]",
				"[\"black\"]",
				"[\"streetwear\"]"));

		assertThat(repository.searchImageIds("embroidered")).isEmpty();
		assertThat(repository.searchImageIds("leather jacket")).containsExactly("image-1");
	}

	@Test
	void indexesManualAnnotationsWithoutDroppingClassificationText() {
		ClassificationRecord classification = classification(
				"image-1",
				"An embroidered linen dress.",
				"[\"dress\"]",
				"[\"cream\"]",
				"[\"artisan\"]");
		repository.updateClassification(classification);
		repository.refreshImage(
				"image-1",
				classification,
				new AnnotationRecord(
						"annotation-1",
						"image-1",
						"[\"capsule reference\",\"neckline detail\"]",
						"Interesting trim for a summer capsule.",
						"Use as a neckline detail reference.",
						null,
						null));

		assertThat(repository.searchImageIds("embroidered")).containsExactly("image-1");
		assertThat(repository.searchImageIds("summer capsule")).containsExactly("image-1");
		assertThat(repository.searchImageIds("neckline detail")).containsExactly("image-1");
	}

	private ClassificationRecord classification(
			String imageId,
			String description,
			String garmentTypeJson,
			String colorPaletteJson,
			String trendNotesJson) {
		return new ClassificationRecord(
				"classification-" + imageId,
				imageId,
				description,
				garmentTypeJson,
				"[]",
				"[]",
				colorPaletteJson,
				"[]",
				"[]",
				"[]",
				"[]",
				trendNotesJson,
				"{}",
				null,
				"{}",
				"test-model",
				null,
				null);
	}
}
