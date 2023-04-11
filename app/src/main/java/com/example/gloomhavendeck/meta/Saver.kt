package com.example.gloomhavendeck.meta

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.Controllable
import com.example.gloomhavendeck.Controller
import com.example.gloomhavendeck.SavableController
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

@Serializable
@RequiresApi(Build.VERSION_CODES.O)
class Saver(private val filesDir: String): Controllable() {

    init {
        Controller.saver = this
    }

    val currentStateSavedAt = Paths.get(filesDir, "current_state.json").toString()

    inline fun <reified T>saveJsonTo(saveMe: T, location: String) {
        val currentStateJson = Json.encodeToString(saveMe)
        File(location).writeText(currentStateJson)
    }

    inline fun <reified T>loadJsonFrom(location: String): T {
        return Controller.let{
            return Json.decodeFromString(File(location).readText())
        }
    }

    fun updateControllerFrom(location: String) {
        val loadedController: SavableController = loadJsonFrom(location)
        Crap.fieldsFromInto(loadedController, Controller)
    }
}