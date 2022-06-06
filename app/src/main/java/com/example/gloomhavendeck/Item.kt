package com.example.gloomhavendeck

enum class Item(val graphic: Int, val sound: Int=R.raw.stone2, val permanent: Boolean =false) {
    CLOAK_OF_POCKETS(R.drawable.card_cloak, permanent = true),
    MAJOR_CURE_POTION(R.drawable.card_cure, sound=R.raw.drinking),
    MAJOR_POWER_POTION(R.drawable.card_power, sound=R.raw.drinking),
    MAJOR_STAMINA_POTION(R.drawable.card_majorstamina, sound=R.raw.drinking),
    MINOR_STAMINA_POTION(R.drawable.card_minorstamina, sound=R.raw.drinking),
    PENDANT_OF_DARK_PACTS(R.drawable.card_pendant),
    RING_OF_BRUTALITY(R.drawable.card_brutality, sound=R.raw.one_more_time),
    RING_OF_SKULLS(R.drawable.card_skulls),
    ROCKET_BOOTS(R.drawable.card_boots),
    SPIKED_SHIELD(R.drawable.card_spiked),
    SUPER_HEALING_POTION(R.drawable.card_healing, sound=R.raw.drinking),
    TOWER_SHIELD(R.drawable.card_tower),
}