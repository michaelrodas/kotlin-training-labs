package org.course.challenge04

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.course.challenge02.AsyncCurrencyService
import org.course.challenge03.StatefulCurrencyConnection
import org.junit.jupiter.api.Test

class Coroutines04ExceptionExerciseTest {

    val bankC = StatefulCurrencyConnection(500, mapOf(AsyncCurrencyService.CHF to 109.12))
    val bankD = StatefulCurrencyConnection(1500, mapOf(AsyncCurrencyService.CHF to 110.2))
    val bankE = StatefulCurrencyConnection(3000, mapOf(AsyncCurrencyService.CHF to 111.3))
    val banks = listOf(bankC, bankD, bankE)

    /**
     * Exercise A:
     *
     * In this exercise you will learn how to handle failing Coroutines in a structured way.
     *
     * Take a look at the implementation of @see StatefulCurrencyConnection, which represents a stateful connection to a Bank that offers currency lookups.
     * In the example below, certain calls to getCurrency(...) throw an Exception. Due to the default error handling behavior of Coroutines, the first
     * failing Coroutine will propagate its Exception, which causes its parent as well as its siblings to be cancelled. Cancellation of the parent and siblings
     * is what we want to prevent in this exercise.
     *
     * Your task is the following one:
     * - Make the code more robust so that one failing Coroutine will not impact the rest. Don't use a try / catch construct for that, but another building block.
     *   Do you remember which one?
     * - In any case - success or failure - we want the resources to be closed, by calling its close() method.
     * - Bonus question: if implemented as intended, joinAll() can be removed. Why is that?
     */
    @Test
    fun `Exercise A handle failing Jobs gracefully so that the overall processing is not affected`(): Unit =
        runBlocking {
            val collectedCurrencies = mutableListOf<Double>()
            val jobs = supervisorScope {
                banks.mapIndexed { idx, conn ->
                    launch {
                        try {
                            val isException = idx % 2 == 0
                            val currency = conn.getCurrency(AsyncCurrencyService.CHF, withException = isException)
                            collectedCurrencies.add(currency)
                        } finally {
                            //coroutine is still healthy, no need for the noncancellable context
                            conn.close()

                        }

                    }
                }
            }
            with(jobs) {
//                joinAll() they are all suspend operations and they are always awaited, that's why this is not required
                filter { it.isCancelled }.size shouldBe 2
                filter { it.isCompleted }.size shouldBe 3
            }
            collectedCurrencies shouldHaveSize 1
            collectedCurrencies.min() shouldBe bankD.getCurrency(AsyncCurrencyService.CHF)
            banks.forEach { it.state shouldBe StatefulCurrencyConnection.Companion.ConnectionState.CLOSED }

        }


    /**
     * Exercise B:
     *
     * In this exercise you will learn how to make async {...} operations failsafe.
     *
     * In the example below some of the getCurrency(...) calls result in an exception. Consider this as a given, like a Network error or something the like
     * you cannot avoid and needs to be dealt with.
     *
     * Your task is the following one:
     * - Create a failsafe version of the code, so that the Exceptions thrown by async{...} will not affect other async {... } calls.
     * - Important: Simply adding a try/catch around the conn.getCurrency(...) call is not a valid solution.
     */
    @Test
    fun `Exercise B handle failing Deferred gracefully so that the overall processing is not affected `(): Unit =
        runBlocking {

            val minRate = supervisorScope {
                banks.mapIndexed { idx, conn ->
                    async {
                        val isException = idx % 2 == 0
                        conn.getCurrency(AsyncCurrencyService.CHF, withException = isException)
                    }

                }
            }.mapNotNull {
                runCatching {
                    it.await()//exceptions might be thrown during await
                }.getOrNull()
            }.min()

            minRate shouldBe bankD.getCurrency(AsyncCurrencyService.CHF)
        }
}