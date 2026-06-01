package com.fashion.backend;

public record ImageDetailResponse(
		ImageRecord image,
		ClassificationRecord classification,
		AnnotationRecord annotation) {
}
