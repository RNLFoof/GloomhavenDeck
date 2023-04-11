package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import com.example.gloomhavendeck.meta.UndoManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RequiresApi(Build.VERSION_CODES.O)
object Controller: SavableController() {
    // I wonder if there's a way to do all these setters in a way that isn't ugly and redundant
    @Transient var undoManager: UndoManager? = null
    @Transient var activityConnector: ActivityConnector? = null

    var enemies: MutableList<Enemy> = mutableListOf()

    fun fullyStock() {
        // Only for testing, really
        // TODO if it's only for testing freaking move it
        Saver("")
        Logger()
        UndoManager()
        Player(26).updateStatus(Status.POISON, 2)
        Inventory().usableItems.add(Item.LUCKY_EYE)
        Deck()
        Pipis()
        enemies.add(Enemy("dog 300 300"))
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