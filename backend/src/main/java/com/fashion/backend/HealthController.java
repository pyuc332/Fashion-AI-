package com.fashion.backend;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

	private final JdbcTemplate jdbcTemplate;

	public HealthController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "ok");
	}

	@GetMapping("/health/db")
	public Map<String, String> databaseHealth() {
		jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		jdbcTemplate.execute("CREATE VIRTUAL TABLE IF NOT EXISTS temp.health_fts_check USING fts5(value)");
		jdbcTemplate.execute("DROP TABLE IF EXISTS temp.health_fts_check");
		return Map.of(
				"database", "ok",
				"fts5", "ok");
	}
}
