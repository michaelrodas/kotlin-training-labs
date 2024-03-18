package org.course.challenge00

import org.springframework.web.bind.annotation.*

/**
 * Challenge 3: A
 * Take a look at the @see JRemoteExchangeController class. Port this Java REST controller
 * to this class using idiomatic Kotlin. Consequently, you have to convert
 * all dependant classes @see RemoteExchangeServe, @see RemoteExchangeDao and @see StockPriceDao into
 * spring beans. Also complete the related data transfer object @see StockQuoteDTO,
 * which should be fairly easy by now. How could you best convert the @see StockQuoteDTO to
 * a @see StockPrice entity?
 * Important: use 'exchange' as endpoint prefix instead of 'jexchange' used in the Java implementation
 */
class RemoteExchangeController(private val remoteExchangeService: RemoteExchangeService) {
    fun getStockPrice(exchangeId:String, symbol:String): StockQuoteDTO = TODO("implement")
    fun putStockPrice( exchangeId: String, @RequestBody stockPrice: StockQuoteDTO): StockQuoteDTO = TODO("implement")
}