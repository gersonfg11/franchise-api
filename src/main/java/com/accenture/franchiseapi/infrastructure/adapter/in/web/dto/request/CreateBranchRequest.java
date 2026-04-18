package com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBranchRequest(@NotBlank String name) {}
