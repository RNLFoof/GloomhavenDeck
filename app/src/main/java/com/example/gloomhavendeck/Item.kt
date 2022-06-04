package com.example.gloomhavendeck

enum class Item(val itemName: String, val graphic: Int, val sound: Int=R.raw.stone2) {
    CLOAK_OF_POCKETS("Cloak of Pockets", R.drawable.card_cloak),
    MAJOR_CURE_POTION("Major Cure Potion", R.drawable.card_cure),
    MAJOR_POWER_POTION("Major Power Potion", R.drawable.card_power),
    MAJOR_STAMINA_POTION("Major Stamina Potion", R.drawable.card_majorstamina),
    MINOR_STAMINA_POTION("Minor Stamina Potion", R.drawable.card_minorstamina),
    PENDANT_OF_DARK_PACTS("Pendant of Dark Pacts", R.drawable.card_pendant),
    RING_OF_BRUTALITY("Ring of Brutality", R.drawable.card_brutality),
    RING_OF_SKULLS("Ring of Skulls", R.drawable.card_skulls),
    ROCKET_BOOTS("Rocket Boots", R.drawable.card_boots),
    SPIKED_SHIELD("Spiked Shield", R.drawable.card_spiked),
    SUPER_HEALING_POTION("Super Healing Potion", R.drawable.card_healing),
    TOWER_SHIELD("Tower Shield", R.drawable.card_tower),
}