package org.course.challenge06

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.course.uitls.prepareTestData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.client.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
class Coroutines03FlowControllerTest @Autowired constructor(val stocksRepository: StocksRepository,
                                                            val webTestClient: WebTestClient,
                                                            @Value("\${remote.service.url}") baseUrl: String,
                                                            @LocalServerPort val localPort: Int) {

    val webClient = WebClient.create(baseUrl.withLocalPort(localPort))

    @BeforeEach
    fun setup():Unit = runBlocking {
        stocksRepository.prepareTestData()
    }

    /**
     * Exercise A:
     * For instructions go to @see StockTraderController#getStocks
     */
    @Test
    fun `Exercise A should get stock by id`(): Unit = runBlocking {
        webTestClient.get().uri("/stocks").exchange().apply {
            expectStatus().isOk
            expectBody<List<Stock>>().returnResult().responseBody shouldBe stocksRepository.findAll().toList()
        }
    }

    /**
     * Exercise B:
     * For instructions go to @see StockTraderController#pollingStocksFlow
     */
    @Test
    @Timeout(5)
    fun `Exercise B should consume newly added Stocks via ServerSentEvents (SSE) emitted from the pollingStockFlow endpoint stocks sse-polling`(): Unit = runBlocking {
        val consumed = mutableListOf<Stock>()
        //we first launch a coroutine to consume stock items, which are added to a MutableList
        val consumerJob = launch {
            webClient.get().uri("/stocks/sse-polling?offset=0").accept(MediaType.TEXT_EVENT_STREAM).exchangeToFlow { it.bodyToFlow<ServerSentEvent<Stock>>() }.collect {
                println("consumed $it")
                it.data()?.let(consumed::add)
            }
        }
        //make sure initial items are consumed
        retryTillOk { consumed shouldBe stocksRepository.findAll().toList() }

        //now we add a new stock to the repository
        val newStock = stocksRepository.save(Stock(symbol = NEWS, price = 2345.12))

        //since the SSE endpoint flow polls newly added items should be consumed
        retryTillOk { consumed shouldContain newStock }

        //cancel job to stop consuming
        consumerJob.cancel()
    }

    /**
     * Exercise C:
     * For instructions go to @see StockTraderController#sharedFlowTriggeredNewsFeedFlow
     */
    @Test
    fun `Exercise C should consume newly added Stocks via ServerSentEvents (SSE) emitted from the pollingStockFlow endpoint stocks sse-shared-flow-triggered`(): Unit = runBlocking {
        val consumed = mutableListOf<Stock>()
        //we first launch a coroutine to consume stock items, which are added to a MutableList
        val consumerJob = launch {
            webClient.get().uri("/stocks/sse-shared-flow-triggered?offset=0").accept(MediaType.TEXT_EVENT_STREAM).exchangeToFlow { it.bodyToFlow<ServerSentEvent<Stock>>() }.collect {
                println("consumed $it")
                it.data()?.let(consumed::add)
            }
        }
        //make sure initial items are consumed
        retryTillOk { consumed shouldBe stocksRepository.findAll().toList() }

        //now we post a new stock via the /stocks endpoint, that should send a StockChangedNotification to the shared flow
        val newStock = webClient.post().uri("/stocks").bodyValue(Stock(symbol = NEWS, price = 2345.12)).awaitExchange { it.awaitBody<Stock>()}

        //the /stocks/sse-shared-flow-triggered endpoint should get a notification via the shared flow by the /stocks endpoint causing it to fetch and emit the newly added Stock
        retryTillOk { consumed shouldContain newStock }

        //cancel job to stop consuming
        consumerJob.cancel()
    }

    companion object {
        fun String.withLocalPort(localPort: Int) = this.replace("8081", localPort.toString())
        suspend fun retryTillOk(delay: Long = 500L, retries: Int = 3, condition: suspend () -> Unit) {
            try {
                condition()
            } catch (ex: Throwable) {
                if (retries > 0) {
                    delay(delay)
                    retryTillOk(delay, retries - 1, condition)
                } else {
                    println("Retries exhausted, test failure due to: ${ex.message}")
                    throw ex
                }
            }
        }

        const val NEWS = "NEWS"
        const val GOOG = "GOOG"
        const val AAPL = "AAPL"
    }
}