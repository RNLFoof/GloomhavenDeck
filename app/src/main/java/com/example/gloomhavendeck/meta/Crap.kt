package com.example.gloomhavendeck.meta

import com.example.gloomhavendeck.Controller
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

open class Crap() {
    companion object {
        inline fun <reified T> fieldsFromInto(from: T, to: T) where T : Any {
            for (property in T::class.memberProperties) {
                if (property is KMutableProperty<*>) {
                    property.setter.call(to, property.get(from))
                }
            }
        }
    }
}