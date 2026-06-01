package com.fashion.backend;

import java.util.List;

public record AnnotationRequest(
		List<String> tags,
		String notes,
		String observations) {
}
