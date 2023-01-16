package com.example.gloomhavendeck.meta

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.*
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

        @RequiresApi(Build.VERSION_CODES.O)
        fun allControllables(): Array<Controllable> {
            // I don't know how to not do this by hand. aaaaaaaa
            return arrayOf(
                Deck(),
                Inventory(),
                Logger(),
                Player(maxHp = 26),
                Saver(filesDir = ""),
                UndoManager(),
                UndoPoint()
            )
        }
    }
}