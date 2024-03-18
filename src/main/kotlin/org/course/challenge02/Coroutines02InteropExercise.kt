package org.course.challenge02

import java.lang.Exception
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface AsyncCurrencyCallback {
    fun onResponse(rate: Double)
    fun onFailure(exception: Exception)
}

class AsyncCurrencyService(private val latency: Long, private val exchangeRates: Map<String, Double>) {

    /**
     *  Exercise A:
     *  See test for instructions
     */
    fun getCurrencyFuture(symbol: String): CompletableFuture<Double> =
            CompletableFuture<Double>().apply {
                executorService.schedule({
                    exchangeRates[symbol]?.let { complete(it) } ?: completeExceptionally(Exception("$symbol not found"))
                }, latency, TimeUnit.MILLISECONDS)
            }

    fun getCurrencyAsync(symbol: String, callback: AsyncCurrencyCallback): Unit {
        executorService.schedule( {
            exchangeRates[symbol]?.let { quote -> callback.onResponse(quote) } ?: callback.onFailure(Exception("$symbol not found"))
        }, latency, TimeUnit.MILLISECONDS)
    }

    companion object {
        val CHF = "CHF"
        val INEXISTANT_CURRENCY = "ERR"
        val executorService = Executors.newScheduledThreadPool(3);
    }
}

/**
 * Exercise B:
 * Take a look at @see AsyncCurrencyService#getCurrencyAsync. It uses a callback interface @see AsyncCurrencyCallback to either set a currency
 * or an exception. The approach can lead to the well-known callback-hell, which is hard to reason about and test. With Coroutines we can do
 * much better: for this exercise implement the extension method AsyncCurrencyService#getCurrencySuspended.
 * Forward the call to AsyncCurrencyService#getCurrencyAsync and map the result of the @see AsyncCurrencyCallback to the Continuation, which
 * can be accessed with some special 'Coroutine glue code'.
 *
 * Tip: The Coroutine glue code involves the helper method suspendCoroutine {...} which gives you access to the underlying Continuation.
 * With the underlying Continuation a success or failure result can be set.
 */
suspend fun AsyncCurrencyService.getCurrencySuspended(symbol: String): Double = suspendCoroutine {
    val callback = object : AsyncCurrencyCallback {
        override fun onResponse(rate: Double) = it.resume(rate)
        override fun onFailure(exception: Exception) = it.resumeWithException(exception)
    }
    getCurrencyAsync(symbol, callback)
}

