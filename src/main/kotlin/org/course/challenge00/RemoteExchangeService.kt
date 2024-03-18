package org.course.challenge00


/**
 * Challenge 2:
 * Take a look at the Java class @see JRemoteExchangeService.
 * Port the logic of all methods from Java to Kotlin using idiomatic Kotlin.
 */
class RemoteExchangeService(val remoteStockPriceDao: RemoteStockPriceDao,
                            val remoteExchangeDao: RemoteExchangeDao
) {

    /**
     * Challenge 1 - Exercise A:
     * Take a look at the Java class @see JRemoteExchangeService#getStockPrice.
     * Port the logic of all methods from Java to Kotlin using idiomatic Kotlin.
     * Remember not to use loops and Optional, but...
     */
    fun getStockPrice(exchangeId: String, symbol: String): StockPrice? =
        TODO("implement")


    /**
     * Challenge 1 - Exercise B:
     * Take a look at the Java class @see JRemoteExchangeService#addStockPrice.
     * Port the logic of all methods from Java to Kotlin using idiomatic Kotlin.
     * Important: For now do not yet implement the defineStockPrice method but keep it as is. Use the
     * standard library method require(...) to do the validation. Try to use a
     * scope method (also) to define the error message and log it. Do you remember
     * where to declare the Logger?
     */
    fun addStockPrice(exchangeId: String, newStockPrice: StockPrice) {
        TODO("implement")
    }

    /**
     * Challenge 1 - Exercise C:
     * Take a look at the Java class @see JRemoteExchangeService#defineStockPrice.
     * Port the logic of all methods from Java to Kotlin using idiomatic Kotlin.
     * In Java, nested if / else statements are used to figure out the price change.
     * Use a when expression in Kotlin and the scope method: 'also' to log the StockPrice
     * in each outcome. Use an expression oriented approach involving no val's or var's.
     *
     * Note 1: In Java the builder method @see JStockPrice.Builder#withNewPrice is used
     * to create a JStockPrice from a current one. In Kotlin we use a simple data class
     * @see StockPrice so there is no need for a builder. How can we best implement the
     * withNewPrice method for @see StockPrice?
     *
     * Note 2: In Java the @see JRemoteExchangeService#changePercentage
     * uses two arguments to calculate the difference in percent between two Doubles.
     * What is the idiomatic way to solve this in Kotlin?
     */
    internal fun defineStockPrice(current: StockPrice?, new: StockPrice): StockPrice {
       return TODO("implement")
    }

}
