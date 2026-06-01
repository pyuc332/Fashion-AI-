package com.fashion.backend;

public record AnnotationRecord(
		String id,
		String imageId,
		String tagsJson,
		String notes,
		String observations,
		String createdAt,
		String updatedAt) {
}
