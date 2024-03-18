package org.course.challenge03

import kotlinx.coroutines.*
import org.course.challenge03.StatefulCurrencyConnection.Companion.ConnectionState.*


class StatefulCurrencyConnection(val latency: Long,  val exchangeRates: Map<String, Double>) {
        private var state_ = INITIALIZED
        val state: ConnectionState
            get() = state_

        suspend fun getCurrency(currency:String, withException:Boolean = false):Double {
            delay(latency)
            return if(withException) throw Exception("Connection with latency=$latency throw exception") else exchangeRates.getValue(currency)
        }
        suspend fun close():Unit {
            delay(1)
            state_ = CLOSED
        }
        companion object {
            enum class ConnectionState {
                INITIALIZED,CLOSED
            }
        }

    }

