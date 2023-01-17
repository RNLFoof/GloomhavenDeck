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

    @Transient var activityConnector: ActivityConnector? = null
    init {
        Thread.dumpStack()
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