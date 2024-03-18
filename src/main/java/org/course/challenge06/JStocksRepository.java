package org.course.challenge06;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface JStocksRepository extends ReactiveCrudRepository<Stock, Long> {

    @Query("select * from stock e where e.id > :id")
    Flux<Stock> findById_GreatherThan(Long id);

    Mono<Stock> findBySymbol(String symbol);
}

