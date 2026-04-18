package com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(@NotBlank String name, @Min(0) int stock) {}
