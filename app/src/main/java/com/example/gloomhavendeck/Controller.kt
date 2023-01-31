package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import com.example.gloomhavendeck.meta.UndoManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    var pipis: Pipis? = null
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

    init {
        // This is true on all the default of all the controlables.
        // It needs a default value, because it's transient, so it doesn't loop forever when saving
        // But those defaults shouldn't ever actually be used
        // Unless it's just to immediately replace
        if (destroyTheUniverseUponInitiation and !suppressUniverseDestruction) {
            throw DestroyTheUniverseUponInitiationException()
        }
    }

    companion object {
        var suppressUniverseDestruction = false

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

        fun doWithoutDestroyingTheUniverse(function: () -> Unit) {
            // dw I think this method of doing it is stupid too
            // I worry about thread safety but the point of universe destruction is just to prevent
            // me from coding something dumb so it should be fine
            // This will, most of the time, warn me of a bug, which is what it needs to do
            suppressUniverseDestruction = true
            val output = function()
            suppressUniverseDestruction = false
            return output
        }

        inline fun <reified T>doWithoutDestroyingTheUniverse(function: () -> T): T {
            // dw I think this method of doing it is stupid too
            // I worry about thread safety but the point of universe destruction is just to prevent
            // me from coding something dumb so it should be fine
            // This will, most of the time, warn me of a bug, which is what it needs to do
            suppressUniverseDestruction = true
            val output = function()
            suppressUniverseDestruction = false
            return output
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

class DestroyTheUniverseUponInitiationException: Exception()