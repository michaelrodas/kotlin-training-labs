package org.course.challenge00

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

/**
 * Challenge 3: B
 * Implement the three tests below making use of the following:
 * - @MockkBean to mock the @see RemoteExchangeService used by the @see RemoteExchangeController
 * - @see MockMvc to execute the http requests. Note: take a look at the provided
 *   testGet(..) and testPut(...), which should help you simplify testing
 * Make sure you inject/declare the mentioned resources into the test class.
 */
@SpringBootTest
class RemoteExchangeControllerTest () {

    /**
     * Implement the test case below
     */
    @Test
    fun `GET to exchanges|id|stocks?symbol=symbol should return stock price`() {
        TODO("implement")
        //1. prepare RemoteExchangeService mock to return a price for mocked getStockPrice
        //2. execute testGet call via MockMvc and assert
        //3. verify RemoteExchangeService mock
    }

    /**
     * Implement the test case below
     */
    @Test
    fun `GET to exchanges|id|stocks?symbol=non-existing-symbol should return 404 if stock cannot be found`() {
        TODO("implement")
        //1. prepare RemoteExchangeService mock to return null for mocked getStockPrice
        //2. execute testGet call via MockMvc and assert
        //3. verify RemoteExchangeService mock
    }

    /**
     * Implement the test case below
     */
    @Test
    fun `PUT to exchanges|id|stocks should add new stock price`() {
        TODO("implement")
        //1. prepare RemoteExchangeService mock to mock addStockPrice
        //2. execute testPut call via MockMvc and assert
        //3. verify RemoteExchangeService mock
    }

    companion object {
        const val NASDAQ = "^IXIC"
        const val GOOG = "GOOG"
    }
}


val OBJECT_MAPPER = jacksonObjectMapper()

fun MockMvc.testGet(uri: String, params: Map<*, *>? = null): ResultActions = perform(
    (params?.let { MockMvcRequestBuilders.get(uri).params(it) } ?: MockMvcRequestBuilders.get(uri))
        .accept(MediaType.APPLICATION_JSON)
)

fun MockMvc.testPut(uri: String, body: Any): ResultActions = perform(
     MockMvcRequestBuilders.put(uri)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(OBJECT_MAPPER.writeValueAsString(body))
)

private fun MockHttpServletRequestBuilder.params(params: Map<*, *>?): MockHttpServletRequestBuilder =
    params?.toList()?.fold(this) { builder, (key, value) ->
        builder.param(key.toString(), value.toString())
    } ?: this

inline fun <reified T> MvcResult.toObject() = OBJECT_MAPPER.readValue<T>(response.contentAsString)
