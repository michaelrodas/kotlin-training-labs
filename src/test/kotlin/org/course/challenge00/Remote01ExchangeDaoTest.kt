package org.course.challenge00

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.*

class Remote01ExchangeDaoTest {


    /**
     * Challenge 1 - Exercise A:
     * For instructions go to @see Exchange and @see JExchange
     */
    @Test
    fun `should have complete Exchange domain object`() {
        Exchange::class.isData shouldBe true
        create<Exchange>(IXIC, NASDAQ, NYC,  USD).apply {
            getValValue("id") shouldBe IXIC
            getValValue("name") shouldBe NASDAQ
            getValValue("location") shouldBe NYC
            getValValue("currency") shouldBe USD
        }
    }

    /**
     * Challenge 1 - Exercise B:
     * Make yourself familiar with the Kotlin extension of the Jackson library needed for Json serialization.
     * First declare a Kotlin compatible ObjectMapper. Then use the mapper
     * to serialize and deserialize the @see sampleExchanges using the convenient Kotlin extension writeValueAsString() and readValue(...)
     * to make the test pass.
     */
    @Test
    fun `should serialize and deserialize Exchanges to and from json`() {
        val mapper: ObjectMapper = TODO("Declare Kotlin specific ObjectMapper")
        val serialized:String = TODO("use mapper to serialize sampleExchanges to String")
        val deserialized:List<Exchange> = TODO("use mapper to deserialize sampleExchanges to a List<Exchange>")
        sampleExchanges shouldBe deserialized
    }

    /**
     * Challenge 1 - Exercise C:
     * For instructions go to @see RemoteExchangeDao#loadExchanges
     * and @see JRemoteExchangeDao#loadExchanges
     */
    @Test
    fun `should load Exchanges from config file`() {
        val mapper = jacksonObjectMapper()
        val exchangesConfigPath = "/exchanges.json"
        with(RemoteExchangeDao(exchangesConfigPath)){

            val exchangeIds =
                mapper.readValue<JsonNode>(this::class.java.getResourceAsStream(exchangesConfigPath)).findValues("id")
                    .map { it.asText() }
            exchangeIds.shouldNotBeEmpty()
            exchangeIds shouldBe findAll().map{it.getValValue("id")}

            exchangeIds.first().let{ firstExchange ->
                exchangeIds.first { it == firstExchange } shouldBe findById(firstExchange)?.getValValue("id")
            }

        }
    }


    companion object {
        const val NASDAQ = "Nasdaq"
        const val IXIC = "^IXIC"
        const val NYC =  "New York City"
        val USD = Currency.getInstance("USD")

        val sampleExchanges by lazy {
            val exchangeServiceNasdaq  = create<Exchange>("^IXIC","Nasdaq", "New York City",
                Currency.getInstance("USD")
            )
            val exchangeServiceEuronext = create<Exchange>("ENX", "Euronext", "Amsterdam", Currency.getInstance("EUR"))
            val exchangeServiceSix = create<Exchange>("SIX", "Swiss Stock Exchange", "Zurich",
                Currency.getInstance("CHF")
            )
            listOf(exchangeServiceEuronext, exchangeServiceNasdaq, exchangeServiceSix)
        }

    }
}