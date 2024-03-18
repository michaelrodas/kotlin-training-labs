package org.course.challenge06

import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.course.challenge06.blocking.BlockingJExchangeService
import org.course.challenge06.blocking.BlockingJExchangeServiceEuronext
import org.course.challenge06.blocking.BlockingJExchangeServiceNasdaq
import org.course.challenge06.blocking.BlockingJExchangeServiceSix
import org.course.challenge06.controller_remote.DummyRemoteExchangeController
import org.course.uitls.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient(timeout = "36000")
class CoroutinesVirtualThreads04ControllerTest @Autowired constructor(
    val stocksRepository: StocksRepository,
    blockingExchangeServiceNasdaq: BlockingJExchangeServiceNasdaq,
    blockingExchangeServiceEuronext: BlockingJExchangeServiceEuronext,
    blockingExchangeServiceSix: BlockingJExchangeServiceSix,
    val webTestClient: WebTestClient,
    @LocalServerPort val localPort: Int
) {

    val exchanges = listOf(blockingExchangeServiceNasdaq, blockingExchangeServiceEuronext, blockingExchangeServiceSix)
    lateinit var initialStocks: List<Stock>
    val webClient = WebClient.create("http://localhost:$localPort")

    val registry = loggingMeterRegistry()
    val okTimer = registry.createTimer("success.timer")
    val nokTimer = registry.createTimer("failure.timer")

    @BeforeEach
    fun setup() = runBlocking {
        initialStocks = stocksRepository.prepareTestData()
        exchanges.forEach {
            it.reAssignVal(BlockingJExchangeService::baseUrl) { it.replace("8081", localPort.toString()) }
        }
    }

    /**
     * Exercise A:
     * For instructions go to @see ClassicStockTraderController#bestQuoteBlocking
     */
    @Test
    fun `Exercise A should fetch best quote for a stock classic endoint in parallel`(): Unit {
        DummyRemoteExchangeController.apply {
            clearMemRepo()
            exchanges.withIndex().forEach { (idx, exchange) -> add(exchange.exchangeId, GOOG, idx.toDouble()) }
        }
        measureTimeMillis {
            webTestClient.get().uri("/classic/stocks/quote?symbol=$GOOG&delay=1000").exchange().run {
                expectStatus().isOk
                expectBody<StockQuoteDto>().returnResult().responseBody.apply {
                    this?.currentPrice shouldBe DummyRemoteExchangeController.get(exchanges.first().exchangeId, GOOG)
                }
            }
        } shouldBeInRange 1000..1500L
    }

    /**
     * Exercise B:
     * In this exercise you will create a performance test that allows you to verify how your application behaves
     * depending on how you configure it with PlatformThreads and/or VirtualThreads.
     * The endpoint we want to test is the same like in the test above: '/classic/stocks/quote?symbol=$GOOG&delay=1000',
     * which is implemented - as you know by know - with blocking io calls.
     * Do the following:
     * - 1. Implement runPerformanceTest(...) method. Here each parameter is explained:
     * -- iterationCount: represents the count of how many times you make calls in parallel to the endpoint above
     * -- startParallelCount: represents the count of the requests you execute in parallel at the beginning of the test
     * -- maxParallelCount: represents how many requests will be executed in parallel anytime
     * -- rampUpCount: represents the amount of parallel calls that will be added to the current parallel count, until maxParallelCount is reached
     * -- callEndpoint: is the function that calls the actual endpoint.
     *    IMPORTANT: callEndpoint must be wrapped in the measure(...) method, which will ensure we get performance metrics
     *
     * - 2. Ensure that the registry.printMetrics() is called every second, so we can see the metrics of our performance test
     * -- The goal is to create a simple asynchronous process that periodically calls the printMetrics() method
     *
     * - 3. Now you should be able to run the performance test
     * -- Use the following configuration initially: iterationCount=20, startParallelCount=50, maxParallelCount=200, rampUpCount=20
     * -- There is a separate log file: perf.log for the performance statistics in the root of this project. Ideally use a terminal with
     *    tail -f perf.log to monitor it.
     * -- Can you reach the maximum parallel count of 200? If not, why could that be ... and what do you need to change to achieve it?
     *
     * - 4. Change the webTestClient to the webClient
     * -- So far you have used the webTestClient to perform the tests, which was blocking. Now let's change the implementation, so
     *    it uses the webClient, which is already pre-configured for you. The webClient is non-blocking. Do you need an additional
     *    (VirtualThread/IO) dispatcher for the webClient or not?
     * -- Run the performance test again and see if you get the desired result depending on the type of Dispatcher you are using
     *
     * - 5. Play around with different configuration
     * -- Try out various configurations to see how they perform. Before executing the test, think how the expected behavior should look like.
     * -- Configuration options:
     * ---a) VirtualThreads configured in SpringBoot, VirtualThreads Dispatcher in classic endpoint, VirtualThreads Dispatcher in test
     * ---b) VirtualThreads configured in SpringBoot, Dispatchers.IO in classic endpoint, VirtualThreads Dispatcher in test
     * ---c) VirtualThreads configured in SpringBoot, VirtualThreads Dispatcher in classic endpoint, Dispatchers.IO in test
     * ---d) VirtualThreads configured in SpringBoot, VirtualThreads Dispatcher in classic endpoint, no dispatcher (EmptyCoroutineContext) in test
     *
     */
    @Test
    fun `Exercise B test performance of different setups`(): Unit = runBlocking {
        //TODO: 2: print metrics every 1 second while the test runs
        registry.printMetrics()

        runPerformanceTest(5, 50, 100, 10){
            webTestClient.get().uri("/classic/stocks/quote?symbol=$GOOG&delay=1000").exchange().expectBody<StockQuoteDto>().returnResult()
        }

    }

    /**
     * 1. Implement runPerformanceTest(...) method. Here each parameter is explained:
     * -- iterationCount: represents the count of how many times you make calls in parallel to the endpoint above
     * -- startParallelCount: represents the count of the requests you execute in parallel at the beginning of the test
     * -- maxParallelCount: represents how many requests will be executed in parallel anytime
     * -- rampUpCount: represents the amount of parallel calls that will be added to the current parallel count, until maxParallelCount is reached
     * -- callEndpoint: is the function that calls the actual endpoint.
     *    IMPORTANT: callEndpoint must be wrapped in the measure(...) method, which will ensure we get performance metrics
     */
    suspend fun runPerformanceTest(
        iterationCount: Int,
        startParallelCount: Int,
        maxParallelCount: Int,
        rampUpCount: Int,
        callEndpoint: suspend () -> Unit
    ): Unit = coroutineScope {
        (1..iterationCount).forEach {
            var currentParallelCount = startParallelCount
            do {
                callEndpoint()
                currentParallelCount++
            }while (currentParallelCount <= maxParallelCount)
        }
    }


    private inline fun <T> measure(block: () -> T) {
        val start = System.currentTimeMillis()
        val result = runCatching { block() }
        val elapsed = System.currentTimeMillis() - start
        (if (result.isFailure) nokTimer else okTimer).record(elapsed, TimeUnit.MILLISECONDS)
    }


    companion object {
        const val NEWS = "NEWS"
        const val GOOG = "GOOG"
        const val AAPL = "AAPL"
    }
}
