package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@RequiresApi(Build.VERSION_CODES.O)
open class UndoPoint(@Transient var controller: Controller? = null) {
    val player = Json.encodeToString(controller!!.player)
    val deck = Json.encodeToString(controller!!.deck)
    val enemies = Json.encodeToString(controller!!.enemies)
    var logCount = controller!!.logCount

    open fun use(controller: Controller) {
        controller.player = Json.decodeFromString(player)
        controller.deck = Json.decodeFromString(deck)
        controller.deck.controller = controller
        controller.enemies = Json.decodeFromString(enemies)
        controller.logsToHide += controller.logCount - logCount
        controller.logCount = logCount
    }
}