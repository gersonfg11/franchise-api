package com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb;

import com.accenture.franchiseapi.application.port.out.FranchiseRepository;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb.entity.FranchiseEntity;
import com.accenture.franchiseapi.infrastructure.adapter.out.dynamodb.mapper.FranchiseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
@RequiredArgsConstructor
public class DynamoDbFranchiseRepository implements FranchiseRepository {

    private final DynamoDbAsyncTable<FranchiseEntity> franchiseTable;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        FranchiseEntity entity = FranchiseMapper.toEntity(franchise);
        return Mono.fromFuture(franchiseTable.putItem(entity))
                .thenReturn(franchise);
    }

    @Override
    public Mono<Franchise> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(franchiseTable.getItem(key))
                .map(FranchiseMapper::toDomain);
    }
}
