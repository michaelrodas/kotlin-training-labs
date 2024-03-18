package org.course.challenge00

import ch.qos.logback.classic.Level
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.course.uitls.registerAppenderForClass
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.random.Random

class Remote02ExchangeServiceTest {


    /**
     * Challenge 2: Exercise A:
     * For instructions go to @see RemoteExchangeService#getStockPrice
     * and @see JRemoteExchangeService#getStockPrice
     */
    @Test
    fun `should return stock price`() {
        (NASDAQ to GOOG).let { (exchange, symbol) ->
            with(RemoteExchangeService(RemoteStockPriceDao(), RemoteExchangeDao())) {
                getStockPrice(exchange, symbol) shouldBe null
                val price = 1.0
                remoteStockPriceDao.save(exchange, create(symbol, price, 0.0, Instant.now()))
                getStockPrice(exchange, symbol)?.price shouldBe price
            }
        }
    }

    /**
     * Challenge 2: Exercise B:
     * For instructions go to @see RemoteExchangeService#addStockPrice
     * and @see JRemoteExchangeService#addStockPrice
     */
    @Test
    fun `should throw IAE if exchange is invalid`() {
        val logs = registerAppenderForClass(RemoteExchangeService::class.java)
        shouldThrow<IllegalArgumentException> {
            RemoteExchangeService(RemoteStockPriceDao(), RemoteExchangeDao()).addStockPrice("BLABLA", create("Oeps", 0.1, 0.0, Instant.now()))
        }
        logs.firstOrNull { it.message == "BLABLA does not exist" }?.level shouldBe Level.ERROR
    }

    /**
     * Challenge 2: Exercise C:
     * For instructions go to @see RemoteExchangeService#defineStockPrice
     * and @see JRemoteExchangeService#defineStockPrice
     */
    @Test
    fun `should add and get stock price`() {
        val logs = registerAppenderForClass(RemoteExchangeService::class.java)
        (NASDAQ to GOOG).let { (exchange, symbol) ->
            with(RemoteExchangeService(RemoteStockPriceDao(), RemoteExchangeDao())) {
                //initially empty
                getStockPrice(exchange, symbol) shouldBe null
                //add price
                val price = Random.nextDouble(10.0, 1000.0)
                addStockPrice(exchange, create(symbol, price, 0.0, Instant.now()))
                logs.last().shouldNotBeNull().message shouldContain ("new stock:")
                getStockPrice(exchange, symbol)?.price shouldBe price

                //increase price
                val increasedPrice = price + 0.1
                addStockPrice(exchange, create(symbol, increasedPrice, 0.0, Instant.now()))
                logs.last().shouldNotBeNull().message shouldContain ("stock increased")
                getStockPrice(exchange, symbol)?.price shouldBe increasedPrice

                //decrease price
                val decreasedPrice = price - 0.1
                addStockPrice(exchange, create(symbol, decreasedPrice, 0.0, Instant.now()))
                logs.last().shouldNotBeNull().message shouldContain ("stock decreased")
                getStockPrice(exchange, symbol)?.price shouldBe decreasedPrice

                //unchanged price
                val unchangedPrice = decreasedPrice
                addStockPrice(exchange, create(symbol, unchangedPrice, 0.0, Instant.now()))
                logs.last().shouldNotBeNull().message shouldContain ("stock no change")
                getStockPrice(exchange, symbol)?.price shouldBe unchangedPrice
            }
        }
    }



    companion object {
        const val NASDAQ = "^IXIC"
        const val SIX = "SIX"
        const val GOOG = "GOOG"

    }
}