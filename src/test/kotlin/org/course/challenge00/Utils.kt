package org.course.challenge00

import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.beInstanceOf
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
