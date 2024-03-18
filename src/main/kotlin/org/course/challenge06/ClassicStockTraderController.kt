package org.course.challenge06

import kotlinx.coroutines.*
import org.course.challenge06.blocking.BlockingJExchangeServiceEuronext
import org.course.challenge06.blocking.BlockingJExchangeServiceNasdaq
import org.course.challenge06.blocking.BlockingJExchangeServiceSix
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.Executors

@RestController
class ClassicStockTraderController(
    val stockRepository: StocksRepository,
    blockingExchangeServiceNasdaq: BlockingJExchangeServiceNasdaq,
    blockingExchangeServiceEuronext: BlockingJExchangeServiceEuronext,
    blockingExchangeServiceSix: BlockingJExchangeServiceSix
) {

    val exchanges = listOf(blockingExchangeServiceEuronext, blockingExchangeServiceNasdaq, blockingExchangeServiceSix)

    /**
     * Challenge 6 - Part 4 - Exercise A
     * In this exercise you will learn how to use VirtualThreads to get the most out of legacy (classic ;-)) code.
     * The below endpoint is almost identical to the /stocks/quote endpoint, except that a blocking Api (RestTemplate)
     * is used to fetch stock quotes rather than the non-blocking WebClient. Take a look at the implementation
     * of one of the BlockingJExchangeServiceXYZ to ensure yourself that the blocking RestTemplate is used.
     *
     * As you might know by now, making blocking calls within a Coroutines has dire consequences. However, by combining
     * VirtualThreads with Coroutines, blocking might not block anymore at all.
     *
     * So your task is the following:
     * - Apply VirtualThreads wherever possible in order to make the test in CoroutinesVirtualThreads04ControllerTest succeed.
     * - The test will ensure that all exchanges endpoints are truly called in parallel.
     * - 'Apply VirtualThreads' is a broad term: where do you need to configure / provide VirtualThreads to have the below
     *    scenario working as desired?
     * - Hints: application.properties and/or Dispatchers...
     *
     * Make the corresponding test in @see CoroutinesVirtualThreads04ControllerTest pass.
     */
    @GetMapping("/classic/stocks/quote", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun bestQuoteBlocking(
        @RequestParam("symbol") symbol: String,
        @RequestParam("delay", required = false) delay: Long? = null
    ): StockQuoteDto =
        withContext(Dispatchers.VT) {
            stockRepository.findBySymbol(symbol)?.let { stock ->
                exchanges.map { async { it.getStockQuote(stock.symbol, delay) } }.awaitAll()
                    .minByOrNull { (it.currentPrice) }
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find stock with symbol=$symbol")
        }

    companion object {
        val Dispatchers.VT
        get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
    }
}
