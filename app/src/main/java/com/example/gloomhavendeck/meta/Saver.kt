package com.example.gloomhavendeck.meta

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.Controllable
import com.example.gloomhavendeck.Controller
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

@Serializable
@RequiresApi(Build.VERSION_CODES.O)
class Saver(override var controller: Controller = Controller(), private val filesDir: String): Controllable() {

    init {
        controller.saver = this
    }

    val currentStateSavedAt = Paths.get(filesDir, "current_state.json").toString()

    fun saveJsonTo(saveMe: Any, location: String) {
        val currentStateJson = Json.encodeToString(saveMe)
        File(location).writeText(currentStateJson)
    }

    inline fun <reified T>loadJsonFrom(location: String): T {
        return Json.decodeFromString<T>(File(location).readText())
    }

    fun updateControllerFrom(location: String) {
        val loadedController = loadJsonFrom<Controller>(location)
        Crap.fieldsFromInto(loadedController, controller)
    }
}