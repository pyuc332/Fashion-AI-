package com.fashion.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

	private final ImageRepository imageRepository;
	private final Path uploadPath;

	public ImageController(ImageRepository imageRepository, @Value("${app.upload-dir}") String uploadDir) {
		this.imageRepository = imageRepository;
		this.uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
	}

	@PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ImageRecord uploadImage(
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) String designer,
			@RequestParam(required = false) String capturedAt,
			@RequestParam(required = false) String continent,
			@RequestParam(required = false) String country,
			@RequestParam(required = false) String city) throws IOException {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are supported.");
		}

		Files.createDirectories(uploadPath);

		String id = UUID.randomUUID().toString();
		String storedFilename = id + extensionFor(file.getOriginalFilename(), contentType);
		Path target = uploadPath.resolve(storedFilename).normalize();
		if (!target.startsWith(uploadPath)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload filename.");
		}

		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

		ImageRecord image = new ImageRecord(
				id,
				cleanOriginalFilename(file.getOriginalFilename()),
				storedFilename,
				"/uploads/" + storedFilename,
				contentType,
				blankToNull(designer),
				blankToNull(capturedAt),
				blankToNull(continent),
				blankToNull(country),
				blankToNull(city),
				null,
				null);

		return imageRepository.save(image);
	}

	@GetMapping
	public List<ImageRecord> listImages() {
		return imageRepository.findAll();
	}

	@GetMapping("/{id}")
	public ImageRecord getImage(@PathVariable String id) {
		return imageRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found."));
	}

	private String extensionFor(String originalFilename, String contentType) {
		String fallback = switch (contentType.toLowerCase(Locale.ROOT)) {
			case "image/png" -> ".png";
			case "image/gif" -> ".gif";
			case "image/webp" -> ".webp";
			default -> ".jpg";
		};
		if (originalFilename == null) {
			return fallback;
		}
		String filename = Path.of(originalFilename).getFileName().toString();
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == filename.length() - 1) {
			return fallback;
		}
		String extension = filename.substring(dotIndex).toLowerCase(Locale.ROOT);
		return extension.matches("\\.[a-z0-9]{1,8}") ? extension : fallback;
	}

	private String cleanOriginalFilename(String originalFilename) {
		if (originalFilename == null || originalFilename.isBlank()) {
			return "upload";
		}
		return Path.of(originalFilename).getFileName().toString();
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
