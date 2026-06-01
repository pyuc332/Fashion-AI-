package com.fashion.backend;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class AnnotationService {

	private final ClassificationParser parser;
	private final AnnotationRepository annotationRepository;
	private final ClassificationRepository classificationRepository;
	private final SearchIndexRepository searchIndexRepository;

	public AnnotationService(
			ClassificationParser parser,
			AnnotationRepository annotationRepository,
			ClassificationRepository classificationRepository,
			SearchIndexRepository searchIndexRepository) {
		this.parser = parser;
		this.annotationRepository = annotationRepository;
		this.classificationRepository = classificationRepository;
		this.searchIndexRepository = searchIndexRepository;
	}

	public AnnotationRecord saveAnnotation(String imageId, AnnotationRequest request) {
		List<String> tags = normalizeTags(request.tags());
		AnnotationRecord annotation = new AnnotationRecord(
				UUID.randomUUID().toString(),
				imageId,
				parser.toJson(tags),
				blankToNull(request.notes()),
				blankToNull(request.observations()),
				null,
				null);
		AnnotationRecord saved = annotationRepository.upsert(annotation);
		searchIndexRepository.refreshImage(
				imageId,
				classificationRepository.findByImageId(imageId).orElse(null),
				saved);
		return saved;
	}

	private List<String> normalizeTags(List<String> tags) {
		if (tags == null) {
			return List.of();
		}
		return tags.stream()
				.filter(tag -> tag != null && !tag.isBlank())
				.map(tag -> tag.trim().toLowerCase(Locale.ROOT))
				.distinct()
				.toList();
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
