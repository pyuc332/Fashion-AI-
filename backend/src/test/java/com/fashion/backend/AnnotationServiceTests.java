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

import com.fasterxml.jackson.databind.ObjectMapper;

class AnnotationServiceTests {

	private Path databasePath;
	private ImageRepository imageRepository;
	private ClassificationRepository classificationRepository;
	private AnnotationRepository annotationRepository;
	private AnnotationService annotationService;
	private SearchIndexRepository searchIndexRepository;

	@BeforeEach
	void setUp() throws Exception {
		databasePath = Files.createTempFile("fashion-annotation-test", ".db");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.sqlite.JDBC");
		dataSource.setUrl("jdbc:sqlite:" + databasePath);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		new DatabaseInitializer(jdbcTemplate).run();
		imageRepository = new ImageRepository(jdbcTemplate);
		classificationRepository = new ClassificationRepository(jdbcTemplate);
		annotationRepository = new AnnotationRepository(jdbcTemplate);
		searchIndexRepository = new SearchIndexRepository(jdbcTemplate);
		ClassificationParser parser = new ClassificationParser(new ObjectMapper());
		annotationService = new AnnotationService(
				parser,
				annotationRepository,
				classificationRepository,
				searchIndexRepository);
	}

	@AfterEach
	void tearDown() throws IOException {
		Files.deleteIfExists(databasePath);
	}

	@Test
	void savesAnnotationAndIndexesManualText() {
		imageRepository.save(new ImageRecord(
				"image-1",
				"image.png",
				"image.png",
				"/uploads/image.png",
				"image/png",
				"Designer A",
				"2026-06-01",
				"North America",
				"United States",
				"Los Angeles",
				null,
				null));

		AnnotationRecord annotation = annotationService.saveAnnotation(
				"image-1",
				new AnnotationRequest(
						List.of("Capsule Reference", "capsule reference", "Neckline Detail"),
						"Interesting trim for a summer capsule.",
						"Use as neckline inspiration."));

		assertThat(annotation.tagsJson()).isEqualTo("[\"capsule reference\",\"neckline detail\"]");
		assertThat(annotation.notes()).contains("summer capsule");
		assertThat(annotationRepository.findByImageId("image-1")).isPresent();
		assertThat(searchIndexRepository.searchImageIds("summer capsule")).containsExactly("image-1");
		assertThat(searchIndexRepository.searchImageIds("neckline detail")).containsExactly("image-1");
	}
}
