package com.example.gloomhavendeck

class Player() {
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
}
