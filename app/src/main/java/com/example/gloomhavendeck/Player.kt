package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
open class Player() {
    var hp = 26
    val usableItems = mutableListOf(
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
    val unusableItems = mutableListOf<Item>()
    val statuses = mutableListOf<Status>()
    var powerPotionThreshold = 6
    var hpDangerThreshold = 10
    var skeletonLocations = 1
    var pierce = 0
    val maxHp = 26

    open fun useItem(item: Item) {
        if (!usableItems.contains(item)) {
            throw Exception("You don't HAVE a $item, dumbass")
        }
        usableItems.remove(item)
        unusableItems.add(item)
        item.getUsed(this)
    }
}
