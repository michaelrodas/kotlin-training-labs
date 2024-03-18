package org.course.challenge00


/**
 * Challenge 1 - Exercise C:
 * Take a look at the Java class @see JRemoteExchangeDao.
 * Port the logic of all methods from Java to Kotlin using idiomatic Kotlin.
 */
class RemoteExchangeDao(path:String = "/exchanges.json") {
    private val exchanges = loadExchanges(path)

    fun findAll() = exchanges

    fun findById(exchangeId: String): Exchange? =
        TODO("implement")

    companion object {
        /**
         * Take a look at the Java class @see JRemoteExchangeDao#loadExchanges.
         * It loads a list of exchanges from file and unmarshalls it to Kotlin.
         * Rewrite the implementation in Kotlin using a Kotlin compatible ObjectMapper
         * and its Kotlin extensions as well as various IO extensions
         * to make the implementation concise and readable. You should be able
         * to implement all the logic in a single expression using the mentioned
         * extensions and a scope method.
         */
        fun loadExchanges(path: String): List<Exchange> =
            TODO("implement")
    }
}


