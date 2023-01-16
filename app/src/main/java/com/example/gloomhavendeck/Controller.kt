package com.example.gloomhavendeck

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import com.example.gloomhavendeck.meta.UndoManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.Callable

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
open class Controller(
) {
    var saver: Saver? = null
    var logger: Logger? = null
    var undoManager: UndoManager?= null
    var player: Player? = null
    var inventory: Inventory? = null
    var deck: Deck? = null
    var enemies: MutableList<Enemy> = mutableListOf()

    companion object {
        fun newFullyStocked(): Controller {
            // Only for testing, really
            val controller = Controller()
            Saver(controller, "")
            Logger(controller)
            UndoManager(controller)
            Player(controller, 26).statusDict[Status.POISON] = 2
            Inventory(controller).usableItems.add(Item.LUCKY_EYE)
            Deck(controller)
            controller.enemies.add(Enemy("dog 300 300"))

            return controller
        }
    }
}