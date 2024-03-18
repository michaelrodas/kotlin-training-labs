package org.course.challenge06

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.course.uitls.prepareTestData
import org.course.uitls.reAssignVal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class Coroutines01ServiceRepositoryTest @Autowired constructor(val stocksRepository: StocksRepository,
                                                               val nasdaqService: ExchangeServiceNasdaq,
                                                               @LocalServerPort val localPort: Int) {

    @BeforeEach
    fun setup():Unit = runBlocking {
        stocksRepository.prepareTestData()
        nasdaqService.reAssignVal(ExchangeServiceNasdaq::baseUrl) { it.replace("8081", localPort.toString()) }
    }

    /**
     * Exercise A:
     * For instructions go to @see StocksRepository
     */
    @Test
    fun `Exercise A should get stock by symbol from Coroutine enabled StocksRepository`(): Unit = runBlocking {
        val foundStock = stocksRepository.findBySymbol(GOOG)
        foundStock?.symbol shouldBe GOOG

    }

    /**
     * Exercise B:
     * For instructions go to @see ExchangeService
     */
    @Test
    fun `Exercise B should get stock quote from ExchangeService`(): Unit = runBlocking {
        val stockQuote = nasdaqService.getStockQuote(GOOG)
        stockQuote.symbol shouldBe GOOG
    }


    companion object {
        const val GOOG = "GOOG"
    }
}