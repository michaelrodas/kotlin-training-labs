package org.course.uitls

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beInstanceOf
import kotlinx.coroutines.flow.toList
import org.course.challenge06.Stock
import org.course.challenge06.StocksRepository
import org.slf4j.LoggerFactory
import java.lang.reflect.Field
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

inline fun <reified T, reified A> T.reAssignVal(fieldName: KProperty1<T, A>, replace: (A) -> A): T {
    val f = recurseField(T::class.java, fieldName.name)
    f.isAccessible = true
    val existingValue = f.get(this)
    f.set(this, replace(existingValue as A))
    return this
}

fun recurseField(clazz: Class<*>, fieldName: String): Field =
        if (clazz == Any::class.java) throw IllegalArgumentException("property $fieldName not found in $clazz") else {
            clazz.declaredFields.find { it.name == fieldName } ?: recurseField(clazz.superclass, fieldName)
        }


fun Any.getValValue(fieldName: String): Any? {
    return this::class.memberProperties.firstOrNull { it.name == fieldName }?.let {
        it shouldNotBe beInstanceOf<KMutableProperty1<*, *>>()
        it.getter.call(this)
    }
}

inline fun <reified T:Any> create(vararg args:Any) = T::class.primaryConstructor?.call(* args) ?: throw IllegalArgumentException("No primary constructor found for type ${T::class}")


suspend fun StocksRepository.prepareTestData(): List<Stock> {
    deleteAll()
    return saveAll(listOf(
            Stock(symbol = "AAPL", price = 124.12),
            Stock(symbol = "MSFT", price = 246.15),
            Stock(symbol = "AMZN", price = 3213.12),
            Stock(symbol = "GOOG", price = 2314.20))).toList()

}

fun <T> registerAppenderForClass(clazz: Class<T>): MutableList<ILoggingEvent> =
    (LoggerFactory.getLogger(clazz) as? Logger)?.let { logger ->
        ListAppender<ILoggingEvent>().apply {
            start()
            logger.addAppender(this)
        }.list
    } ?: mutableListOf()

