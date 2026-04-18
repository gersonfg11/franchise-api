package com.accenture.franchiseapi.application.service;

import com.accenture.franchiseapi.application.port.in.FranchiseUseCase;
import com.accenture.franchiseapi.application.port.out.FranchiseRepository;
import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.infrastructure.adapter.in.web.dto.response.TopProductResponse;
import com.accenture.franchiseapi.infrastructure.exception.BranchNotFoundException;
import com.accenture.franchiseapi.infrastructure.exception.FranchiseNotFoundException;
import com.accenture.franchiseapi.infrastructure.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FranchiseService implements FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    @Override
    public Mono<Franchise> addFranchise(String name) {
        Franchise franchise = Franchise.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .branches(new ArrayList<>())
                .build();
        return franchiseRepository.save(franchise);
    }

    @Override
    public Mono<Branch> addBranch(String franchiseId, String branchName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = Branch.builder()
                            .id(UUID.randomUUID().toString())
                            .name(branchName)
                            .products(new ArrayList<>())
                            .build();
                    franchise.getBranches().add(branch);
                    return franchiseRepository.save(franchise).thenReturn(branch);
                });
    }

    @Override
    public Mono<Product> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new BranchNotFoundException(branchId));

                    Product product = Product.builder()
                            .id(UUID.randomUUID().toString())
                            .name(productName)
                            .stock(stock)
                            .build();
                    branch.getProducts().add(product);
                    return franchiseRepository.save(franchise).thenReturn(product);
                });
    }

    @Override
    public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new BranchNotFoundException(branchId));

                    boolean removed = branch.getProducts().removeIf(p -> p.getId().equals(productId));
                    if (!removed) {
                        return Mono.error(new ProductNotFoundException(productId));
                    }
                    return franchiseRepository.save(franchise).then();
                });
    }

    @Override
    public Mono<Product> updateProductStock(String franchiseId, String branchId, String productId, int newStock) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new BranchNotFoundException(branchId));

                    Product product = branch.getProducts().stream()
                            .filter(p -> p.getId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new ProductNotFoundException(productId));

                    product.setStock(newStock);
                    return franchiseRepository.save(franchise).thenReturn(product);
                });
    }

    @Override
    public Flux<TopProductResponse> getTopStockProductsByBranch(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMapMany(franchise -> Flux.fromIterable(franchise.getBranches()))
                .flatMap(branch -> Mono.justOrEmpty(
                        branch.getProducts().stream()
                                .max(Comparator.comparingInt(Product::getStock))
                                .map(product -> new TopProductResponse(
                                        branch.getId(),
                                        branch.getName(),
                                        product.getId(),
                                        product.getName(),
                                        product.getStock()
                                ))
                ));
    }

    @Override
    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(franchise -> {
                    franchise.setName(newName);
                    return franchiseRepository.save(franchise);
                });
    }

    @Override
    public Mono<Branch> updateBranchName(String franchiseId, String branchId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new BranchNotFoundException(branchId));

                    branch.setName(newName);
                    return franchiseRepository.save(franchise).thenReturn(branch);
                });
    }

    @Override
    public Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException(franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = franchise.getBranches().stream()
                            .filter(b -> b.getId().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new BranchNotFoundException(branchId));

                    Product product = branch.getProducts().stream()
                            .filter(p -> p.getId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new ProductNotFoundException(productId));

                    product.setName(newName);
                    return franchiseRepository.save(franchise).thenReturn(product);
                });
    }
}
