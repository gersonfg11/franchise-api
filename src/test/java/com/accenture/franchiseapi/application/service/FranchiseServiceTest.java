package com.accenture.franchiseapi.application.service;

import com.accenture.franchiseapi.application.port.out.FranchiseRepository;
import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.infrastructure.exception.BranchNotFoundException;
import com.accenture.franchiseapi.infrastructure.exception.FranchiseNotFoundException;
import com.accenture.franchiseapi.infrastructure.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private FranchiseService franchiseService;

    private static final String FRANCHISE_ID = "franchise-1";
    private static final String BRANCH_ID = "branch-1";
    private static final String PRODUCT_ID = "product-1";

    private Product product;
    private Branch branch;
    private Franchise franchise;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(PRODUCT_ID).name("Big Mac").stock(100).build();
        branch = Branch.builder().id(BRANCH_ID).name("Sucursal Norte").products(new ArrayList<>(List.of(product))).build();
        franchise = Franchise.builder().id(FRANCHISE_ID).name("McDonalds").branches(new ArrayList<>(List.of(branch))).build();
    }

    // ---- addFranchise ----

    @Test
    void addFranchise_shouldReturnSavedFranchise() {
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.addFranchise("McDonalds"))
                .assertNext(f -> {
                    assertThat(f.getName()).isEqualTo("McDonalds");
                    assertThat(f.getId()).isNotBlank();
                    assertThat(f.getBranches()).isEmpty();
                })
                .verifyComplete();
    }

    // ---- addBranch ----

    @Test
    void addBranch_shouldAddBranchToFranchise() {
        Franchise emptyFranchise = Franchise.builder().id(FRANCHISE_ID).name("McDonalds").branches(new ArrayList<>()).build();
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(emptyFranchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.addBranch(FRANCHISE_ID, "Sucursal Norte"))
                .assertNext(b -> {
                    assertThat(b.getName()).isEqualTo("Sucursal Norte");
                    assertThat(b.getId()).isNotBlank();
                    assertThat(b.getProducts()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void addBranch_shouldFailWhenFranchiseNotFound() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.addBranch(FRANCHISE_ID, "Sucursal Norte"))
                .expectError(FranchiseNotFoundException.class)
                .verify();
    }

    // ---- addProduct ----

    @Test
    void addProduct_shouldAddProductToBranch() {
        Franchise franchiseWithEmptyBranch = Franchise.builder()
                .id(FRANCHISE_ID).name("McDonalds")
                .branches(new ArrayList<>(List.of(
                        Branch.builder().id(BRANCH_ID).name("Sucursal Norte").products(new ArrayList<>()).build()
                ))).build();
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseWithEmptyBranch));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.addProduct(FRANCHISE_ID, BRANCH_ID, "Big Mac", 50))
                .assertNext(p -> {
                    assertThat(p.getName()).isEqualTo("Big Mac");
                    assertThat(p.getStock()).isEqualTo(50);
                    assertThat(p.getId()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void addProduct_shouldFailWhenBranchNotFound() {
        Franchise franchiseNoBranch = Franchise.builder()
                .id(FRANCHISE_ID).name("McDonalds").branches(new ArrayList<>()).build();
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchiseNoBranch));

        StepVerifier.create(franchiseService.addProduct(FRANCHISE_ID, BRANCH_ID, "Big Mac", 50))
                .expectError(BranchNotFoundException.class)
                .verify();
    }

    // ---- deleteProduct ----

    @Test
    void deleteProduct_shouldRemoveProductFromBranch() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.deleteProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                .verifyComplete();

        assertThat(branch.getProducts()).isEmpty();
    }

    @Test
    void deleteProduct_shouldFailWhenProductNotFound() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseService.deleteProduct(FRANCHISE_ID, BRANCH_ID, "non-existent-product"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    // ---- updateProductStock ----

    @Test
    void updateProductStock_shouldUpdateStock() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.updateProductStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 999))
                .assertNext(p -> assertThat(p.getStock()).isEqualTo(999))
                .verifyComplete();
    }

    @Test
    void updateProductStock_shouldFailWhenFranchiseNotFound() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.updateProductStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 999))
                .expectError(FranchiseNotFoundException.class)
                .verify();
    }

    // ---- getTopStockProductsByBranch ----

    @Test
    void getTopStockProductsByBranch_shouldReturnTopProductPerBranch() {
        Product lowStock = Product.builder().id("p2").name("McFlurry").stock(30).build();
        branch.getProducts().add(lowStock);

        Branch branch2 = Branch.builder().id("branch-2").name("Sucursal Sur")
                .products(new ArrayList<>(List.of(
                        Product.builder().id("p3").name("Nuggets").stock(200).build()
                ))).build();
        franchise.getBranches().add(branch2);

        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseService.getTopStockProductsByBranch(FRANCHISE_ID))
                .assertNext(r -> {
                    assertThat(r.branchId()).isEqualTo(BRANCH_ID);
                    assertThat(r.productName()).isEqualTo("Big Mac");
                    assertThat(r.stock()).isEqualTo(100);
                })
                .assertNext(r -> {
                    assertThat(r.branchId()).isEqualTo("branch-2");
                    assertThat(r.productName()).isEqualTo("Nuggets");
                    assertThat(r.stock()).isEqualTo(200);
                })
                .verifyComplete();
    }

    @Test
    void getTopStockProductsByBranch_shouldFailWhenFranchiseNotFound() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(franchiseService.getTopStockProductsByBranch(FRANCHISE_ID))
                .expectError(FranchiseNotFoundException.class)
                .verify();
    }

    // ---- updateFranchiseName ----

    @Test
    void updateFranchiseName_shouldUpdateName() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.updateFranchiseName(FRANCHISE_ID, "Burger King"))
                .assertNext(f -> assertThat(f.getName()).isEqualTo("Burger King"))
                .verifyComplete();
    }

    // ---- updateBranchName ----

    @Test
    void updateBranchName_shouldUpdateName() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.updateBranchName(FRANCHISE_ID, BRANCH_ID, "Sucursal Sur"))
                .assertNext(b -> assertThat(b.getName()).isEqualTo("Sucursal Sur"))
                .verifyComplete();
    }

    @Test
    void updateBranchName_shouldFailWhenBranchNotFound() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseService.updateBranchName(FRANCHISE_ID, "non-existent-branch", "Nuevo"))
                .expectError(BranchNotFoundException.class)
                .verify();
    }

    // ---- updateProductName ----

    @Test
    void updateProductName_shouldUpdateName() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(franchiseRepository.save(any(Franchise.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseService.updateProductName(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "McChicken"))
                .assertNext(p -> assertThat(p.getName()).isEqualTo("McChicken"))
                .verifyComplete();
    }

    @Test
    void updateProductName_shouldFailWhenProductNotFound() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseService.updateProductName(FRANCHISE_ID, BRANCH_ID, "non-existent-product", "McChicken"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }
}
