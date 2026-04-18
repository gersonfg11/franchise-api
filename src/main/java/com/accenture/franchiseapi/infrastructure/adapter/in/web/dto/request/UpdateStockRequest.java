package com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateStockRequest(@Min(0) int stock) {}
