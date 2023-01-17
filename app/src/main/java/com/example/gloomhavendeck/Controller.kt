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
open class Controller(var destroyTheUniverseUponInitiation: Boolean = false
) {
    // I wonder if there's a way to do all these setters in a way that isn't ugly and redundant
    var saver: Saver? = null
        set(value) {
            if (value != null && value.controller != this) {value.controller = this}
            field = value
        }
    var logger: Logger? = null
        set(value) {
            if (value != null && value.controller != this) {value.controller = this}
            field = value
        }
    var player: Player? = null
        set(value) {
            if (value != null && value.controller != this) {value.controller = this}
            field = value
        }
    var inventory: Inventory? = null
        set(value) {
            if (value != null && value.controller != this) {value.controller = this}
            field = value
        }
    var deck: Deck? = null
        set(value) {
            if (value != null && value.controller != this) {value.controller = this}
            field = value
        }
    @Transient var undoManager: UndoManager?= null
        set(value) {
            if (value != null && value.controller != this) {value.controller = this}
            field = value
        }
    @Transient var activityConnector: ActivityConnector? = null
        set(value) {
            if (value != null && value.controller != this) {value.controller = this}
            field = value
        }

    var enemies: MutableList<Enemy> = mutableListOf()

    @Transient var activityConnector: ActivityConnector? = null
    init {
        // This is true on all the default of all the controlables.
        // It needs a default value, because it's transient, so it doesn't loop forever when saving
        // But those defaults shouldn't ever actually be used
        // Unless it's just to immediately replace
        if (destroyTheUniverseUponInitiation) {
            throw destroyTheUniverseUponInitiationException()
        }
    }

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

    fun sortEnemies(enemyOrder: List<String>) {
        val nameRegex = Regex("[a-z]+", RegexOption.IGNORE_CASE)
        enemies = enemies.sortedBy { it.name }.toMutableList()
        enemies = enemies.sortedBy {
            val name = nameRegex.find(it.name)!!.value
            if (name in enemyOrder) {
                enemyOrder.indexOf(name)
            } else {
                -1
            }
        }.toMutableList()
    }
}

class destroyTheUniverseUponInitiationException: Exception() {

}