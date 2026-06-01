package com.fashion.backend;

public record ImageRecord(
		String id,
		String originalFilename,
		String storedFilename,
		String imageUrl,
		String contentType,
		String designer,
		String capturedAt,
		String continent,
		String country,
		String city,
		String createdAt,
		String updatedAt) {
}
