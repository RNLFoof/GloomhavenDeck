package com.example.gloomhavendeck
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable

@RequiresApi(Build.VERSION_CODES.N)
@Serializable
open class Inventory {
    var usableItems = mutableListOf(
        Item.CLOAK_OF_POCKETS,
        Item.MAJOR_CURE_POTION,
        Item.MAJOR_POWER_POTION,
        Item.MAJOR_STAMINA_POTION,
        Item.MINOR_STAMINA_POTION,
        Item.PENDANT_OF_DARK_PACTS,
        Item.RING_OF_BRUTALITY,
        Item.RING_OF_SKULLS,
        Item.ROCKET_BOOTS,
        Item.SPIKED_SHIELD,
        Item.SUPER_HEALING_POTION,
        Item.TOWER_SHIELD,
    )
    var activeItems = mutableListOf<Item>()
    var unusableItems = mutableListOf<Item>()

    fun loseItem(item: Item) {
        if (!usableItems.contains(item)) {
            throw Exception("Don't have a $item in usable!")
        }
        usableItems.remove(item)
        unusableItems.add(item)
    }

    fun regainItem(item: Item) {
        if (!unusableItems.contains(item)) {
            throw Exception("Don't have a $item in unusable!")
        }
        unusableItems.remove(item)
        usableItems.add(item)
    }

    open fun useItem(player: Player, item: Item) {
        if (!usableItems.contains(item)) {
            throw Exception("You don't HAVE a $item, dumbass")
        }
        item.getUsed(player)
        loseItem(item)
    }
}