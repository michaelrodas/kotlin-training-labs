package org.course.challenge00

import java.util.concurrent.ConcurrentHashMap

class RemoteStockPriceDao {

    private val stockPriceMemRepo = ConcurrentHashMap<ExchangeStockSymbol, StockPrice>()

    fun findById(exchangeId: String): List<StockPrice> =
        stockPriceMemRepo.filter { (key) -> key.exchange == exchangeId }.values.toList()

    fun save(exchangeId: String, stockPrice: StockPrice) {
        stockPriceMemRepo[ExchangeStockSymbol(exchangeId, stockPrice.symbol)] = stockPrice
    }

    fun clearMemRepo() =
        stockPriceMemRepo.clear()


}