package org.course.challenge03

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.course.challenge02.AsyncCurrencyService
import org.course.challenge02.AsyncCurrencyService.Companion.CHF
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.measureTimeMillis

class Coroutines03CancellationExerciseTest {

    val bankC = StatefulCurrencyConnection(500, mapOf(AsyncCurrencyService.CHF to 109.12))
    val bankD = StatefulCurrencyConnection(1500, mapOf(AsyncCurrencyService.CHF to 110.2))
    val bankE = StatefulCurrencyConnection(3000, mapOf(AsyncCurrencyService.CHF to 111.3))
    val banks = listOf(bankC, bankD, bankE)


    /**
     * Exercise A:
     *
     * In this exercise you will explore cancellation.
     * Take a look at the implementation of @see StatefulCurrencyConnection, which represents a stateful connection to a Bank that offers currency lookups.
     *
     * For this exercise, we will only use the getCurrency(...) method to fetch a currency, the close() method will not be used yet.
     *
     * Your task is the following one:
     * - Fetch the CHF currency of all banks in parallel using launch{...}.
     * - Store the fetched currency in the predefined collectedCurrencies MutableList<Double>.
     * - The fetching must not take longer than 2000 ms. Therefore, *manually* cancel all jobs that are not completed yet after 2000 ms.
     * - Tip: do not use withTimeout(...). Solely rely on a combination of delay(...) and cancel().
     * - Bonus question: use a separate launch{...} that cancels the non-completed jobs after 2000 ms. How can you ensure that the
     *   test assertions still succeed, which means that they should be triggered *after* the cancellation launch {... } has completed?
     */
    @Test
    fun `Exercise A fetch all currencies in parallel within max 2000 ms`() {
        runBlocking {
            val collectedCurrencies = mutableListOf<Double>()
//val job = launch{
            val jobs: List<Job> = banks.map {
                launch {
                    collectedCurrencies.add(it.getCurrency(CHF))
                }

            }
            delay(2000)
            //job.cancel()
            launch { jobs.forEach { it.cancel() } }

            collectedCurrencies shouldHaveSize 2
            collectedCurrencies.min() shouldBe bankC.getCurrency(AsyncCurrencyService.CHF)
        }
    }

    /**
     * Exercise B:
     *
     * In this exercise you will learn how to clean up resources even if a job is cancelled.
     * The cleanup code that needs to be executed, comes in the form of the  @see StatefulCurrencyConnection.close() method.
     *
     * Your task is the following one:
     * - After having fetched a currency, the connection needs to be closed by calling the close() method.
     * - The connection also needs to be closed when the job is cancelled.
     * - Important 1: be aware that the close() method is a suspend method. Do you remember what you need to do to ensure that it will be called even if a job is cancelled?
     * - Important 2: even though you might have succeeded with cancelling the connection, the test assertion that checks for the connection state still might fail.
     *               How can you ensure that *all* Coroutines are completed before you check for the connection state? Tip: coroutineScope {...} might come in handy...
     */
    @Test
    fun `Exercise B fetch all currencies in parallel within max 2000 ms and close all connections even if one is cancelled`(): Unit =
        runBlocking {
            coroutineScope {
                val collectedCurrencies = mutableListOf<Double>()
                val jobs: List<Job> = banks.map {
                    launch {
                        try {
                            val currency = it.getCurrency(AsyncCurrencyService.CHF)
                            collectedCurrencies.add(currency)
                        } finally {
                            withContext(NonCancellable) {
                                it.close()
                            }
                        }
                    }
                }
                delay(2000)
                jobs.forEach { it.cancel() }

                collectedCurrencies shouldHaveSize 2
                collectedCurrencies.min() shouldBe bankC.getCurrency(AsyncCurrencyService.CHF)
                banks.forEach { it.state shouldBe StatefulCurrencyConnection.Companion.ConnectionState.CLOSED }
            }
        }


    /**
     * Exercise C:
     *
     * In this exercise you have to ensure that cancellation behaves in a cooperative way.
     *
     * Take a look at the code snippet below: Instead of using the suspend getCurrency(...) method,
     * the snippet makes use of a blocking variation, named getCurrencyBlocking(...).
     * When you run the test, you will see that it will take forever - if it was not terminated by the @Timeout annotation after 5 seconds.
     *
     * Your task is the following one:
     * - Fix this code so that the blocking call is becoming cooperative with the Coroutine cancellation mechanism.
     * - You have to use getCurrencyBlocking(...), so you are not allowed to replace it with getCurrency(...).
     * - In addition, you also have to ensure that the connection is closed when the Coroutine is cancelled, by calling close().
     * - Tip: the latter might be quite tricky: a good start might be catching the CancellationException, and call close() in the catch block.
     *   But that might not be enough...
     */
    @Test
    @Timeout(5L)
    fun `Exercise C infinitely fetch currencies in parallel using a blocking call that is cooperative with cancellation`(): Unit =
        runBlocking {

            fun StatefulCurrencyConnection.getCurrencyBlocking(currency: String): Double {
                Thread.sleep(latency)
                return exchangeRates.getValue(currency)
            }

            val bestCurrency = AtomicReference<Double?>()
            val job = launch(Dispatchers.IO) {
                banks.map {
                    launch {
                        while (true) {
                            try {
                                ensureActive()
                                val currency =
                                    it.getCurrencyBlocking(AsyncCurrencyService.CHF) //do NOT replace this call with getCurrency
                                bestCurrency.getAndUpdate { existing -> if (existing != null && currency > existing) existing else currency }
                                //TODO 1: make the blocking call cooperative with Coroutine cancellation
                                //TODO 2: implement closing of the connection only in case of cancellation
                                yield()
                            } catch (e: CancellationException) {
                                withContext(NonCancellable) {
                                    it.close()
                                }
                                throw e
                            }
                        }
                    }
                }
            }
            delay(2000)
            job.cancelAndJoin()
            bestCurrency.get() shouldBe bankC.getCurrency(AsyncCurrencyService.CHF)
            banks.forEach { it.state shouldBe StatefulCurrencyConnection.Companion.ConnectionState.CLOSED }
        }

    /**
     * Bonus Exercise D:
     *
     * In this bonus exercise you will use async/await combined with a timeout.
     * Your task is the following one:
     * - Call the getCurrency(...) method of all banks in parallel using async{...}.
     * - If a call to getCurrency(...) takes longer than 2000 ms it must be cancelled. Tip: use withTimeout(...) or withTimeoutOrNull(...) to accomplish that.
     * - The close() method must always be called, no matter whether the call returned a value or was cancelled.
     * - Calculate the minimum currency rate you have fetched within the 2000 seconds.
     */
    @Test
    fun `Exercise D fetch all currencies in parallel within max 2000 ms and close all connections even if one is cancelled`(): Unit =
        runBlocking {
            val elapsed = measureTimeMillis {
                val minRate: Double = banks.map {
                    async {
                        try{
                        withTimeoutOrNull(2000) {
                            it.getCurrency(CHF)
                        }}
                        finally {
                            withContext(NonCancellable) {
                                it.close()
                            }
                        }
                    }
                }.awaitAll().filterNotNull().min()
                //mapNotnull {await()}.min

                minRate shouldBe bankC.getCurrency(AsyncCurrencyService.CHF)
                banks.forEach { it.state shouldBe StatefulCurrencyConnection.Companion.ConnectionState.CLOSED }
            }
            elapsed.toDouble() shouldBe 2000.toDouble().plusOrMinus(700.0)
        }
}