package org.course.challenge01

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import org.course.challenge01.CurrencyService.Companion.USD
import org.course.uitls.registerAppenderForClass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.system.measureTimeMillis

class Coroutines01CoroutineContextExerciseTest {

    val bankA = CurrencyService(500, mapOf(CurrencyService.USD to 125.12))
    val bankB = CurrencyService(1000, mapOf(CurrencyService.USD to 126.2))
    val bankC = CurrencyService(1500, mapOf(CurrencyService.USD to 124.12))
    val banks = listOf(bankA, bankB, bankC)

    @BeforeEach
    fun setup(){
        logs.clear()
    }

    /**
     * Exercise A:
     * Let's prove how much more resource efficient Coroutines are:
     * In this exercise call @see CurrencyService#getCurrency of all banks concurrently with a twist:
     * - In the Coroutine builder you have to pass a CoroutineDispatcher that has a ThreadPool with only a single Thread.
     * This singleThreadPool CoroutineDispatcher is already provided, you only have to use it.
     * Don't forget to compare the purely Thread based example in the beginning of this test with
     * your Coroutine solution.
     *
     * Can you see how much more performance Coroutines offer you compared to the Threading example?
     */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @Test
    fun `Exercise A should call getCurrency(USD) methods of all banks using a threadpool with a single thread`() {
        val singleThreadPool = newSingleThreadContext("single-thread-pool")
        //The following is purely informative to better understand the differences/drawbacks of Threads vs Coroutines:
        //Threading example using the single-thread-pool:
        val latch = CountDownLatch(banks.size)
        val threadsMs = measureTimeMillis {
            banks.forEach { bank ->
                singleThreadPool.executor.execute {
                    bank.getCurrencyBlocking(CurrencyService.USD).also { latch.countDown() }
                }
            }
            latch.await()
        }
        //as you can see with a single thread, performance suffers because getCurrencyBlocking blocks the Thread in sleep, yielding the performance as if these methods were called sequentially
        threadsMs shouldBeGreaterThanOrEqual (500 + 1000 + 1500)

        //with Coroutines you can do much better:
        //even though we only have one thread it is used much more efficient because suspend functions don't block the Thread that executes them
        val coroutinesMs = measureTimeMillis {
            //TODO: call CurrencyService.getCurrency(USD) of all banks using the singleThreadPool as input of the coroutine builder method runBlocking
            runBlocking(newSingleThreadContext("single-thread-pool")) {
                banks.forEach {
                    launch {
                        it.getCurrency(USD)
                    }
                }
                Thread.currentThread().name shouldStartWith "single-thread-pool"
            }
        }
        //important: the total time should not be greater (+/-) than the latency of the slowest service (here bankC)
        coroutinesMs.toDouble() shouldBe 1500.toDouble().plusOrMinus(400.0)

    }


    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @Test
    /**
     * Exercise B:
     * In this exercise you will learn how to deal with blocking calls when using Coroutines.
     * Your task is to retrieve the conversion rate of all banks in parallel and retrieve the lowest conversion rate.
     * Do you remember which Coroutine builder you have to use to achieve parallelism?
     *
     * However, there is a twist: you must call the *blocking* getCurrencyBlocking(...) method of the bank's @see CurrencyService
     * and NOT the suspend getCurrency(...) method.
     *
     * The challenge is twofold:
     * - you have to ensure that all the calls to getCurrencyBlocking(...) will be *really* executed in parallel.
     * - you also have to ensure that the MDC context (called req-id) is propagated properly to the Coroutines you spawn.
     *
     * This scenario you can easily encounter in real-world applications: Sometimes we have to call blocking code
     * when we use Coroutines. It is therefore important, that you know how to deal with this particular case.
     **/
    fun `Exercise B should take the lowest rate of all banks executing the blocking call to getCurrencyBlocking(USD) in parallel using async`() {
        val coroutinesMs = measureTimeMillis {
            val requestId = UUID.randomUUID().toString()
            MDC.put("req-id", requestId)

            //TODO: provide the correct CoroutineContexts to ensure that:
            //- the parallel calls to getCurrencyBlocking(USD) is not handled with the single Thread from runBlocking{...{
            //- the MDC context is propagated to the coroutines doing the getCurrencyBlocking(USD) call
            runBlocking(MDCContext() + Dispatchers.IO) {

                //TODO: call all bank's getCurrencyBlocking(USD) method in parallel and retrieve the lowest rate
                // Important! DO NOT not use the suspend getCurrency(...) method
                val minRateOfAllBanks = banks.map { async { it.getCurrencyBlocking(USD) } }.awaitAll().min()
                val expectedRates = listOf(125.12, 126.2, 124.12)

                //assert min rate
                minRateOfAllBanks shouldBe expectedRates.min()
                //assert logging and MDC propagation
                expectedRates.forEach { rate ->
                    val logMessage = logs.firstOrNull().shouldNotBeNull().also { it.message == "Get currency blocking ${CurrencyService.USD} at rate $rate" }.shouldNotBeNull()
                    logMessage.mdcPropertyMap["req-id"] shouldBe requestId
                }
            }
        }
        //important: the total time should not be greater (+/-) than the latency of the slowest service (here bankC)
        coroutinesMs.toDouble() shouldBe 1500.toDouble().plusOrMinus(400.0)
    }

    companion object {
        val logs = registerAppenderForClass(CurrencyService::class.java)
    }

}