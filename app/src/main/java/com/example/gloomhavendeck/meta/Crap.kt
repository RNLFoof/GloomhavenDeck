package com.example.gloomhavendeck.meta

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.gloomhavendeck.*
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

open class Crap() {
    companion object {

        inline fun <reified T> fieldsFromInto(from: T, to: T) where T : Any {
            for (property in T::class.memberProperties) {
                if (property is KMutableProperty<*>) {
                    if (property.get(from) != null) {
                        property.setter.call(to, property.get(from))
                    }
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

        fun crashProtector(activity: Activity, function: () -> Unit = {}) {
            try {
                function()
            } catch (e: Exception) {
                activity.runOnUiThread {
                    val dialogBuilder = AlertDialog.Builder(activity)
                    dialogBuilder.setMessage(e.stackTraceToString())
                    dialogBuilder.setPositiveButton("Ignore") {_,_ ->}
                    dialogBuilder.setNegativeButton("Crash the app lmao") {_,_ -> throw e}
                    val alert = dialogBuilder.create()
                    alert.setTitle("OW?")
                    alert.show()
                }
            }
        }


        fun getResourceAsText(path: String): String {
            var output = {}.javaClass.getResource(path)?.readText()
            if (output == null) {
                val file = File("src/main/" + path)
                output = file.readText()
            }
            return output
        }


    }
}