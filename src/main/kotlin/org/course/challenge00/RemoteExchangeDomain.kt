package org.course.challenge00

import java.time.Instant


/**
 * Challenge 1 - Exercise A:
 * Take a look at the java class @see JExchange. Extend the @see Exchange class below
 * so that it possesses the same attributes as @see JExchange. Don't forget to favor immutability.
 * Hint: you don't have to port the Builder from Java, but instead use a 'special' type of class,
 * which offers builder functionality and more out of the box.
 */
class Exchange()


/**
 * Challenge 1 - Exercise C:
 * Take a look at the method @see JStockPrice.Builder#withNewPrice. Since with use a data class
 * @see StockPrice in Kotlin there is no need for a builder. How can we best implement the
 * withNewPrice method?
 *
 * Note: In Java the @see JRemoteExchangeService#changePercentage
 * uses two arguments to calculate the difference in percent between two Doubles.
 * What is the idiomatic way to solve this in Kotlin?
 *
 */
data class StockPrice(val symbol: String, val price: Double, val changePercentage:Double = 0.0, val lastChange: Instant = Instant.now())

data class ExchangeStockSymbol(val exchange: String, val symbol: String)


/**
 * Challenge 3 - Exercise A:
 * Take a look at the java class @see JStockQuoteDTO. Extend the @see StockQuoteDTO class below
 * so that it possesses the same attributes as @see JStockQuoteDTO. Don't forget to favor immutability.
 * Hint: you don't have to port the Builder from Java, but instead use a 'special' type of class,
 * which offers builder functionality and more out of the box.
 */
class StockQuoteDTO()