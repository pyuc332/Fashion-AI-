package com.fashion.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/filters")
public class FilterController {

	private final FilterService filterService;

	public FilterController(FilterService filterService) {
		this.filterService = filterService;
	}

	@GetMapping
	public FilterOptions getFilters() {
		return filterService.getFilterOptions();
	}
}
