package com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.response;

public record TopProductResponse(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock
) {}
