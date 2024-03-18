package org.course.challenge06

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table


@Table("stocks")
data class Stock(
        @Id
        val id: Long? = null,
        val symbol: String,
        val price: Double,
)


data class StockOrderDto(val symbol: String,
                         val count: Int)

data class StockQuoteDto(val symbol: String,
                         val currentPrice: Double)