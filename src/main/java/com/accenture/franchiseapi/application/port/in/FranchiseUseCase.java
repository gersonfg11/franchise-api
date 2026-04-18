package com.accenture.franchiseapi.application.port.in;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.response.TopProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchiseUseCase {
    Mono<Franchise> addFranchise(String name);
    Mono<Branch> addBranch(String franchiseId, String branchName);
    Mono<Product> addProduct(String franchiseId, String branchId, String productName, int stock);
    Mono<Void> deleteProduct(String franchiseId, String branchId, String productId);
    Mono<Product> updateProductStock(String franchiseId, String branchId, String productId, int newStock);
    Flux<TopProductResponse> getTopStockProductsByBranch(String franchiseId);
    Mono<Franchise> updateFranchiseName(String franchiseId, String newName);
    Mono<Branch> updateBranchName(String franchiseId, String branchId, String newName);
    Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String newName);
}
