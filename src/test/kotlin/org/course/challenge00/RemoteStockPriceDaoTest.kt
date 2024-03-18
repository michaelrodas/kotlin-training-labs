package org.course.challenge00

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.random.Random

class RemoteStockPriceDaoTest {


    @Test
    fun `should have complete StockPrice domain object`() {
        val now = Instant.now()
        StockPrice::class.isData shouldBe true
        create<StockPrice>(GOOG, 10.0, 0.0, now).apply {
            getValValue("symbol") shouldBe GOOG
            getValValue("price") shouldBe 10.0
            getValValue("changePercentage") shouldBe 0.0
            getValValue("lastChange") shouldBe now
        }
    }


    @Test
    fun `should have complete ExchangeStockSymbol domain object`() {
        ExchangeStockSymbol::class.isData shouldBe true
        create<ExchangeStockSymbol>(NASDAQ, GOOG).apply {
            getValValue("exchange") shouldBe NASDAQ
            getValValue("symbol") shouldBe GOOG
        }
    }

    @Test
    fun `should add and get stock price`() {
        (NASDAQ to GOOG).let { (exchange, symbol) ->
            with(RemoteStockPriceDao()) {
                //initially empty
                findById(exchange).shouldBeEmpty()
                //add price
                val price = Random.nextDouble(10.0, 1000.0)
                save(exchange, create(symbol, price, 0.0, Instant.now()))
                findById(exchange).first().getValValue("price") shouldBe price
                //clear and check is empty
                clearMemRepo()
                findById(exchange).shouldBeEmpty()
            }
        }
    }


    companion object {
        const val NASDAQ = "^IXIC"
        const val GOOG = "GOOG"

    }
}