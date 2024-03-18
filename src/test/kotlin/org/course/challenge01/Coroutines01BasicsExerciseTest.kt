package org.course.challenge01

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.course.challenge01.CurrencyService.Companion.USD
import org.course.uitls.registerAppenderForClass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread
import kotlin.reflect.full.functions
import kotlin.system.measureTimeMillis


class Coroutines01BasicsExerciseTest {

    val bankA = CurrencyService(500, mapOf(USD to 125.12))
    val bankB = CurrencyService(1000, mapOf(USD to 126.2))
    val bankC = CurrencyService(1500, mapOf(USD to 124.12))
    val banks = listOf(bankA, bankB, bankC)

    @BeforeEach
    fun setup() {
        logs.clear()
    }

    /**
     * Exercise A:
     * Take a look at @see CurrencyService$getCurrencyBlocking. It simulates latency by using Thread.sleep(...), which will
     * block the Thread. With Coroutines we can do better: Implement the method @see CurrencyService#getCurrency
     * by converting it into a suspend function and call delay instead of Thread.sleep so that no Thread will be blocked
     * any time.
     * Make this test succeed.
     */
    @Test
    fun `Exercise A should convert CurrencyService getCurrency method into suspend function replacing sleep with delay`() {
        val suspended = CurrencyService::class.functions
            .filter { it.name == "getCurrency" }
            .all { it.isSuspend }
        suspended shouldBe true
    }


    /**
     * Exercise B:
     * Now it's time to use the newly implemented method @see CurrencyService#getCurrency.
     * Call this method in the test below. Make sure you provide a CoroutineScope using the right
     * Coroutine builder.
     */
    @Test
    fun `Exercise B should call CurrencyService getCurrency(USD) method of bankA with correct coroutine builder `() {
        //TODO: call the newly implemented method CurrencyService.getCurrency(USD) for bankA .
        val currency = runBlocking {
            bankA.getCurrency(USD)
        }
        currency shouldBe 125.12

        logs.firstOrNull { it.message.contains("Get currency $USD at rate 125.12") }.shouldNotBeNull()
    }


    /**
     * Exercise C:
     * Let's throw some concurrency into the mix to experience what 'structured concurrency' feels like.
     * In this exercise call  @see CurrencyService#getCurrency of bankA and bankB concurrently by
     * using the Coroutine builder launch.
     * Don't forget to compare the purely Thread based example in the beginning of this test with
     * your Coroutine solution.
     */
    @Test
    fun `Exercise C should call getCurrency(USD) method of bankA and bankB concurrently with using the coroutine builder launch `() {
        //The following is purely informative to better understand the differences/drawbacks of Threads vs Coroutines:
        //Threading example requires manual joining of threads using join()
        val threadsMs = measureTimeMillis {
            val threadA = thread { bankA.getCurrencyBlocking(USD) }
            val threadB = thread { bankB.getCurrencyBlocking(USD) }
            threadA.join() //<- needed when using threads
            threadB.join() //<- needed when using threads
        }
        //important: the total time should not be greater (+/-) than the latency of the slowest service (here bankB)
        threadsMs.toDouble() shouldBe 1000.toDouble().plusOrMinus(300.0)


        //with coroutines you can do much better: no manual joining is needed due to structured concurrency
        val coroutinesMs = measureTimeMillis {
            runBlocking {
                //TODO: call CurrencyService.getCurrency(USD) of bankA and bankB concurrently using the coroutine builder launch
                launch { bankA.getCurrency(USD) }
                launch { bankB.getCurrency(USD) }
            } //<- at the end of runBlocking all Coroutines will have been joined 'automagically' due to structured concurrency,
        }
        //important: the total time should not be greater (+/-) than the latency of the slowest service (here bankB)
        coroutinesMs.toDouble() shouldBe 1000.toDouble().plusOrMinus(300.0)

    }


    /**
     * Exercise D:
     * Finally, take a look how easy it is to leverage parallelism using the Coroutine builder methods async / await:
     * In this exercise call @see CurrencyService#getCurrency of all banks in parallel using async / await and calculate
     * the average conversion rate of all the results.
     */
    @Test
    fun `Exercise D should calculate the average rate of all banks executing the call to getCurrency(USD) in parallel using async`() {
        val coroutinesMs = measureTimeMillis {
            //TODO: calculate the average conversion rate of all backs executing the call to getCurrency(USD) in parallel using async and await
            runBlocking {
                val averageRateOfAllBanks: Double =
                    banks.map { async { it.getCurrency(USD) } }.awaitAll().average()
                averageRateOfAllBanks shouldBe listOf(125.12, 126.2, 124.12).average()
            }
        }
        //important: the total time should not be greater (+/-) than the latency of the slowest service (here bankC)
        coroutinesMs.toDouble() shouldBe 1500.toDouble().plusOrMinus(400.0)
    }

    companion object {
        val logs = registerAppenderForClass(CurrencyService::class.java)
    }

}