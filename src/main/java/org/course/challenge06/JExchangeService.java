package org.course.challenge06;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public abstract  class JExchangeService {

    private final WebClient webClient;
    private final String exchangeId;

    protected JExchangeService(String baseUrl, String exchangeId) {
        this.webClient = WebClient.create(baseUrl);
        this.exchangeId = exchangeId;
    }

    public Mono<StockQuoteDto> getStockQuote(String stockSymbol, @Nullable Long delay) {
        return
                webClient.get()
                        .uri("/quotes?symbol=" + stockSymbol + "&exchange=" + exchangeId + (delay != null ? "&delay=" + delay : ""))
                        .retrieve()
                        .bodyToMono(StockQuoteDto.class);
    }

    public Mono<StockQuoteDto> getStockQuote(String stockSymbol) {
        return getStockQuote(stockSymbol, null);
    }
}
