package com.accenture.franchiseapi.application.port.out;

import com.accenture.franchiseapi.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {
    Mono<Franchise> save(Franchise franchise);
    Mono<Franchise> findById(String id);
}
