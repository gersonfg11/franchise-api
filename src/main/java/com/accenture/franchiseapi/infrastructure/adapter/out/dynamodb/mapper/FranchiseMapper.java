package com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb.mapper;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb.entity.BranchEntity;
import com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb.entity.FranchiseEntity;
import com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb.entity.ProductEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FranchiseMapper {

    private FranchiseMapper() {}

    public static FranchiseEntity toEntity(Franchise franchise) {
        FranchiseEntity entity = new FranchiseEntity();
        entity.setId(franchise.getId());
        entity.setName(franchise.getName());
        entity.setBranches(franchise.getBranches().stream()
                .map(FranchiseMapper::toBranchEntity)
                .collect(Collectors.toList()));
        return entity;
    }

    public static Franchise toDomain(FranchiseEntity entity) {
        return Franchise.builder()
                .id(entity.getId())
                .name(entity.getName())
                .branches(entity.getBranches() == null ? new ArrayList<>() :
                        entity.getBranches().stream()
                                .map(FranchiseMapper::toBranchDomain)
                                .collect(Collectors.toList()))
                .build();
    }

    private static BranchEntity toBranchEntity(Branch branch) {
        BranchEntity entity = new BranchEntity();
        entity.setId(branch.getId());
        entity.setName(branch.getName());
        entity.setProducts(branch.getProducts().stream()
                .map(FranchiseMapper::toProductEntity)
                .collect(Collectors.toList()));
        return entity;
    }

    private static Branch toBranchDomain(BranchEntity entity) {
        return Branch.builder()
                .id(entity.getId())
                .name(entity.getName())
                .products(entity.getProducts() == null ? new ArrayList<>() :
                        entity.getProducts().stream()
                                .map(FranchiseMapper::toProductDomain)
                                .collect(Collectors.toList()))
                .build();
    }

    private static ProductEntity toProductEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setStock(product.getStock());
        return entity;
    }

    private static Product toProductDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .stock(entity.getStock())
                .build();
    }
}
