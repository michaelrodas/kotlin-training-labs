package org.course.challenge01

import kotlinx.coroutines.delay
import org.course.utils.logger

class CurrencyService(private val latency:Long, private val exchangeRates:Map<String, Double>) {

    fun getCurrencyBlocking(currency: String): Double {
        Thread.sleep(latency)
        return exchangeRates.getOrDefault(currency, 0.0).also{
            logger.info("Get currency blocking $currency at rate $it")
        }
    }

    /**
     * Exercise A: implement getCurrency similar to getCurrencyBlocking but then without blocking
     * a Thread. Make it suspend and do not use sleep to simulate a delay but instead use: ...
     * IMPORTANT: Don't forget to log the call with EXACTLY the following message: 'Get currency $currency at rate $rate'
     * (otherwise your tests won't succeed)
     */
    suspend fun getCurrency(currency: String): Double {
        delay(latency)
        return exchangeRates.getOrDefault(currency, 0.0).also{
            logger.info("Get currency $currency at rate $it")
        }
    }


    companion object {
        val USD = "USD"
    }
}