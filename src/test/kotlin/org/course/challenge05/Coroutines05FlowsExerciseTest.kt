package org.course.challenge05

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.math.BigInteger
import kotlin.system.measureTimeMillis

class Coroutines05FlowsExerciseTest {

    /**
     * Exercise A:
     * Let's get started with your first flow. In this exercise you have to create a simple flow that
     * emits two String's being: 'hello' and 'flow'.
     */
    @Test
    fun `Exercise A should create a flow that produces two strings 'hello' and 'flow'`(): Unit = runBlocking {
        //TODO implement a simple flow that emits two String's: 'hello' and 'flow'
        val flow: Flow<String> = flow {
            emit("hello")
            emit("flow")
        }

        flow.toList() shouldBe listOf("hello", "flow")
    }

    /**
     * Exercise B:
     * The previous simple flow was finite: now let's create an (theoretically) infinite flow that generates
     * the fibonacci sequence forever. You might know fibonacci from Scrum estimation session, where complexity points
     * reflect the fibonacci sequence (1,2,3,5,8,13 etc.).
     * For more information about fibonacci see: https://www.mathsisfun.com/numbers/fibonacci-sequence.html.
     * To complete this exercise implement an infinite flow that emits all values of the fibonacci sequence.
     * Between every emission the flow needs to delay for 50ms.
     * Hint: The helper method `generateSequence` might come in handy to create the fibonacci sequence and use 'asFlow()' to convert the Sequence to a Flow.
     */
    @Test
    @Timeout(2)
    fun `Exercise B should create flow generating fibonacci sequence with a delay of 50 ms`(): Unit = runBlocking {
        //TODO implement an infinite flow that emits all values of the fibonacci sequence. Between every emission the flow needs to delay for 50ms.
        // For more information about fibonacci see: https://www.mathsisfun.com/numbers/fibonacci-sequence.html

        val flow: Flow<BigInteger> = generateSequence(
            BigInteger.ZERO to BigInteger.ONE)
            { (first, second) -> second to (first.add(second))}.map { it.first }
            .asFlow().onEach { delay(50) }

        val millis = measureTimeMillis {
            flow.take(9).toList() shouldBe listOf(0, 1, 1, 2, 3, 5, 8, 13, 21).map { BigInteger.valueOf(it.toLong()) }
        }
        millis.toDouble() shouldBe (9 * 50).toDouble().plusOrMinus(200.0)
    }


    /**
     * Exercise C:
     * Now we are ready to implement a flow that interacts with a resource, like a data access object.
     * See @see NewsFeedService#pollingNewsFeedFlow() for instructions
     */
    @Test
    @Timeout(5)
    fun `Exercise C should create pollingNewsFeedFlow that initially fetches all data via findByIdGreaterThan and then keeps on polling for new items in intervals of 200ms `(): Unit =
        runBlocking {
            val dao = NewsFeedDao((1..10).map { NewsItem(it.toLong()) }.toMutableList())

            //TODO implement: see: NewsFeedService.pollingNewsFeedFlow() for instructions
            val flow: Flow<NewsItem> = NewsFeedService(dao).pollingNewsFeedFlow()

            val millis = measureTimeMillis {
                val result = mutableListOf<NewsItem>()
                //launch a coroutine to consume news items, which are added to a MutableList
                val job = launch {
                    flow.collect {
                        println("consumed $it")
                        result.add(it)
                    }
                }
                //make sure initial items are consumed
                retryTillOk {
                    result.toList().maxByOrNull { it.id }?.id shouldBe 10
                }

                //now we add new news items to dao
                dao.addAll((11..15).map { NewsItem(it.toLong()) })

                //since the flow polls newly added items should be consumed
                retryTillOk {
                    result.toList().apply {
                        this.size shouldBe 15
                        this.maxByOrNull { it.id }?.id shouldBe 15
                    }
                }
                //cancel job to stop consuming
                job.cancel()
            }
            millis.toDouble() shouldBe (1500).toDouble().plusOrMinus(500.0)
        }


    /**
     * Exercise D:
     * Let's combine a flow with a SharedFlow.
     * See @see NewsFeedService#sharedFlowTriggeredNewsFeedFlow() for instructions
     */
    @Test
    @Timeout(5)
    fun `Exercise E should create sharedFlowTriggeredNewsFeedFlow that initially fetches all data via findByIdGreaterThan and later fetches new items by a notification emitted to the MutableSharedFlow`(): Unit =
        runBlocking {
            val dao = NewsFeedDao((1..10).map { NewsItem(it.toLong()) }.toMutableList())

            val sharedFlow = MutableSharedFlow<String>()
            //TODO implement: see: NewsFeedService.sharedFlowTriggeredNewsFeedFlow() for instructions
            val flow: Flow<NewsItem> = NewsFeedService(dao).sharedFlowTriggeredNewsFeedFlow(sharedFlow)

            val millis = measureTimeMillis {
                val result = mutableListOf<NewsItem>()
                //launch a coroutine to consume news items, which are added to a MutableList
                val job = launch {
                    flow.collect {
                        println("consumed $it")
                        result.add(it)
                    }
                }
                //make sure initial items are consumed
                retryTillOk {
                    result.toList().maxByOrNull { it.id }?.id shouldBe 10
                }

                //now we add new news items to dao
                dao.addAll((11..15).map { NewsItem(it.toLong()) })
                //...and send a notification via the sharedFlow to notify flow that new news should be fetched
                sharedFlow.emit("New News available")

                //consume newly added items
                retryTillOk {
                    result.toList().apply {
                        this.size shouldBe 15
                        this.maxByOrNull { it.id }?.id shouldBe 15
                    }
                }
                //cancel job to stop consuming
                job.cancel()
            }
            millis.toDouble() shouldBe (1500).toDouble().plusOrMinus(1000.0)
        }

    companion object {
        suspend fun retryTillOk(delay: Long = 500L, retries: Int = 3, condition: () -> Unit) {
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
    }
}