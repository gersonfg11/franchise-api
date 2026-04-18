package com.accenture.franchiseapi.infrastructure.adapter.in.web;

import com.accenture.franchiseapi.application.port.in.FranchiseUseCase;
import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request.CreateBranchRequest;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request.CreateFranchiseRequest;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request.CreateProductRequest;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request.UpdateNameRequest;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.request.UpdateStockRequest;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.response.TopProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/franchises")
@RequiredArgsConstructor
public class FranchiseController {

    private final FranchiseUseCase franchiseUseCase;

    // Criterio 2: Agregar nueva franquicia
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franchise> addFranchise(@Valid @RequestBody CreateFranchiseRequest request) {
        return franchiseUseCase.addFranchise(request.name());
    }

    // Criterio 3: Agregar nueva sucursal a la franquicia
    @PostMapping("/{franchiseId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Branch> addBranch(
            @PathVariable String franchiseId,
            @Valid @RequestBody CreateBranchRequest request) {
        return franchiseUseCase.addBranch(franchiseId, request.name());
    }

    // Criterio 4: Agregar nuevo producto a la sucursal
    @PostMapping("/{franchiseId}/branches/{branchId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> addProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody CreateProductRequest request) {
        return franchiseUseCase.addProduct(franchiseId, branchId, request.name(), request.stock());
    }

    // Criterio 5: Eliminar producto de una sucursal
    @DeleteMapping("/{franchiseId}/branches/{branchId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId) {
        return franchiseUseCase.deleteProduct(franchiseId, branchId, productId);
    }

    // Criterio 6: Modificar stock de un producto
    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/stock")
    public Mono<Product> updateProductStock(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        return franchiseUseCase.updateProductStock(franchiseId, branchId, productId, request.stock());
    }

    // Criterio 7: Producto con más stock por sucursal para una franquicia
    @GetMapping("/{franchiseId}/top-products")
    public Flux<TopProductResponse> getTopStockProductsByBranch(@PathVariable String franchiseId) {
        return franchiseUseCase.getTopStockProductsByBranch(franchiseId);
    }

    // Plus: Actualizar nombre de franquicia
    @PatchMapping("/{franchiseId}/name")
    public Mono<Franchise> updateFranchiseName(
            @PathVariable String franchiseId,
            @Valid @RequestBody UpdateNameRequest request) {
        return franchiseUseCase.updateFranchiseName(franchiseId, request.name());
    }

    // Plus: Actualizar nombre de sucursal
    @PatchMapping("/{franchiseId}/branches/{branchId}/name")
    public Mono<Branch> updateBranchName(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody UpdateNameRequest request) {
        return franchiseUseCase.updateBranchName(franchiseId, branchId, request.name());
    }

    // Plus: Actualizar nombre de producto
    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/name")
    public Mono<Product> updateProductName(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateNameRequest request) {
        return franchiseUseCase.updateProductName(franchiseId, branchId, productId, request.name());
    }
}
