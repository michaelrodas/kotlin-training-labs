package org.course.challenge06

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.supervisorScope
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
class StockTraderController(
    val stockRepository: StocksRepository,
    exchangeServiceNasdaq: ExchangeServiceNasdaq,
    exchangeServiceEuronext: ExchangeServiceEuronext,
    exchangeServiceSix: ExchangeServiceSix
) {

    val exchanges = listOf(exchangeServiceEuronext, exchangeServiceNasdaq, exchangeServiceSix)

    /**
     * Challenge 6 - Part 2 - Exercise A
     * Take a look at the reactive @see JStockTraderController#getStock Java implementation,
     * which makes use of the reactive JStocksRepository to fetch a stock by id from the database.
     * In this exercise you have to convert the Java implementation to its Coroutine counterpart
     * by using the injected stockRepository. Remember not to use the Mono abstraction at all.
     * Make the corresponding test in @see Coroutines02ControllerTest pass.
     */
    @GetMapping("/stocks/{stock-id}")
    @ResponseBody
    suspend fun getStock(@PathVariable("stock-id") id: Long?): Stock? {
        return stockRepository.findById(id!!)
    }


    /**
     * Challenge 6 - Part 2 - Exercise B
     * Take a look at the reactive @see JStockTraderController#getStock Java implementation,
     * which makes use of the reactive JStocksRepository to fetch a stock by symbol from the database.
     * In this exercise you have to convert the Java implementation to its Coroutine counterpart
     * by using the injected stockRepository. Remember not to use the Mono abstraction at all.
     * Make the corresponding test in @see Coroutines02ControllerTest pass.
     */
    @GetMapping("/stock")
    @ResponseBody
    suspend fun stockBySymbol(@RequestParam("symbol") symbol: String): Stock? =
        stockRepository.findBySymbol(symbol)


    /**
     * Challenge 6 - Part 2 - Exercise C
     * Take a look at the reactive @see JStockTraderController#upsertStock Java implementation,
     * which makes use of the reactive JStocksRepository to upsert a stock in the database.
     * In this exercise you have to convert the Java implementation to its Coroutine counterpart
     * by using the injected stockRepository. Remember not to use the Mono abstraction at all.
     * Make the corresponding test in @see Coroutines02ControllerTest pass.
     *
     * (In Challenge 6 - Part 3 - Exercise C you will have to extend this method)
     */
    @PostMapping("/stocks")
    @ResponseBody
    suspend fun upsertStock(@RequestBody stock: Stock): Stock? {
        val toUpdate = stock.id?.let { stockRepository.findById(stock.id) }?.copy(price = stock.price)
        return toUpdate?.let { stockRepository.save(toUpdate) }

//        if (stock.id == null) return stockRepository.save(stock)
//        return stockRepository.findById(stock.id)
//            .apply { StockBuilder.from(this).withPrice(stock.price).build() }
//            .also { stockRepository.save(it as Stock) }
    }

    /**
     * Challenge 6 - Part 2 - Exercise D
     * Take a look at the reactive @see JStockTraderController#bestQuote Java implementation,
     * which makes use of the reactive JExchangeServices to fetch several quotes for a stock and
     * filters out the one with the lowest price.
     * In this exercise you have to convert the Java implementation to its Coroutine counterpart
     * by using the injected exchanges. Remember not to use the Mono abstraction at all.
     * Tip: to call the different exchanges in parallel make use of async / await. Don't forget
     * to use the helper method coroutineScope { } for async to work.
     * Make the corresponding test in @see Coroutines02ControllerTest pass.
     */
    @GetMapping("/stocks/quote")
    @ResponseBody
    suspend fun bestQuote(@RequestParam("symbol") symbol: String, delay: Long?): StockQuoteDto? =
        supervisorScope {
            stockRepository.findBySymbol(symbol)?.let {
                exchanges.map {
                    async {
                        it.getStockQuote(
                            symbol,
                            delay
                        )
                    }
                }.awaitAll().minBy { it.currentPrice }
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find stock with symbol=$symbol")
        }


//        return coroutineScope {
//            val stock = async { stockRepository.findBySymbol(symbol) }
//            exchanges.map {
//                async { it.getStockQuote(
//                    stock.await()!!.symbol,
//                    0
//                )}
//            }.awaitAll().minBy { it.currentPrice  }
//        }


    //========================================================================================================

    /**
     * Challenge 6 - Part 3 - Exercise A
     * Take a look at the reactive @see JStockTraderController#getStocks Java implementation,
     * which makes use of the reactive JStockRepository to fetch all stocks as a Flux.
     * In this exercise you have to convert the Java implementation to its Coroutine counterpart
     * by using the injected StockRepository. Remember to use a Flow and not the Flux abstraction at all.
     * Make the corresponding test in @see Coroutines03FlowControllerTest pass.
     */
    @GetMapping("/stocks")
    @ResponseBody
    suspend fun getStocks(): Flow<Stock> = stockRepository.findAll()


    /**
     * Challenge 6 - Part 3 - Exercise B
     * In this exercise you will apply (almost) the same logic as in Challenge 4 Exercise C, where you implemented
     * a flow that was polling a resources in intervals of 200ms and returned newly added items. Now
     * you will take this implementation to the next level: you will poll a 'real' database resource and
     * return newly added Stocks in a ServerSentEvent<Stock> stream, so that potentially the whole world
     * can consume your flow.
     * Therefore, implement an infinite flow that initially fetches all Stocks via StockRepository.findById_GreaterThan(...)
     * and then keeps on polling for new Stocks in intervals of 200ms
     * Hint 0: re-visit @see NewsFeedService#pollingNewsFeedFlow. The implementation is almost identical
     * Hint 1: Since StockRepository.findById_GreaterThan(...) will return a Flow that you have to collect
     * in the flow that will serve the SSE items. So you will get a Flow within a Flow which is perfectly fine.
     * Also use a var latestId in the flow builder to keep track of the latestId that was fetched, which is perfectly thread-safe.
     * Hint 2: It might help defining a nested method (e.g.: tailrec suspend fun fetch()) that recursively fetches
     * NewsItem based on the latestId it accesses in the surrounding scope. Once fetched it also delay's for 200ms before recursing.
     */
    @GetMapping("/stocks/sse-polling", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun pollingStocksFlow(@RequestParam("offset") offsetId: Long = 0): Flow<ServerSentEvent<Stock>> = flow {
        var latestId = offsetId
        tailrec suspend fun fetch() { //this means kotlin optimizes recursion
            stockRepository.findById_GreaterThan(latestId).collect {
                emit(
                    ServerSentEvent.builder<Stock>()
                        .id(it.id.toString())
                        .event("Stock")
                        .data(it).build()
                )
                latestId = it.id!!
            }
            delay(200)
            fetch()
        }
        fetch()
    }


    data class StockChangedNotification(val stock: Stock)

    private val sharedFlow = MutableSharedFlow<StockChangedNotification>()

    /**
     * Challenge 6 - Part 3 - Exercise C
     * In this exercise you will apply (almost) the same logic as in Challenge 5 Exercise E, where you implemented
     * a flow that was first fetching all items of a resources and then waited for a notification being emitted by the SharedFlow to fetch and return newly added items.
     * Also, here the implementation is almost identical to the previous one with the following differences:
     * - First extend the above @see StockTraderController#upsertStock(...) method that will emit a @StockChangedNotification to the predefined MutableSharedFlow.
     * - Second instead of polling for new items, let the flow wait for a StockChangedNotification (via the SharedFlow.collect{...} method ) and then initiate a new fetch
     * Hint 1: re-visit @see NewsFeedService#sharedFlowTriggeredNewsFeedFlow. The implementation is almost identical.
     */
    @GetMapping("/stocks/sse-shared-flow-triggered", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    fun sharedFlowTriggeredNewsFeedFlow(@RequestParam("offset") offsetId: Long = 0): Flow<ServerSentEvent<Stock>> =
        flow {
            var latestId = offsetId
            suspend fun fetch() { //this means kotlin optimizes recursion
                stockRepository.findById_GreaterThan(latestId).collect {
                    emit(
                        ServerSentEvent.builder<Stock>()
                            .id(it.id.toString())
                            .event("Stock")
                            .data(it).build()
                    )
                    it.id?.also { id -> latestId = id }
                }
            }
            fetch()
            sharedFlow.collect { fetch() }
        }

}

