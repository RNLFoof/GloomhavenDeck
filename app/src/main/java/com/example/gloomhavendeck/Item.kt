package com.example.gloomhavendeck
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Integer.min

@RequiresApi(Build.VERSION_CODES.N)
enum class Item(val graphic: Int, val sound: Int=R.raw.stone2, val permanent: Boolean=false,
    val getUsed: (player: Player) -> Unit = {}) {
    CLOAK_OF_POCKETS(R.drawable.card_cloak, permanent = true),
    MAJOR_CURE_POTION(R.drawable.card_cure, sound=R.raw.drinking, getUsed={
        if (!it.statuses.any{it.negative}) {
            throw kotlin.Exception("No negative statuses!")
        }
        it.statuses.removeIf{it.negative}
    }),
    MAJOR_POWER_POTION(R.drawable.card_power, sound=R.raw.drinking),
    MAJOR_STAMINA_POTION(R.drawable.card_majorstamina, sound=R.raw.drinking),
    MINOR_STAMINA_POTION(R.drawable.card_minorstamina, sound=R.raw.drinking),
    PENDANT_OF_DARK_PACTS(R.drawable.card_pendant),
    RING_OF_BRUTALITY(R.drawable.card_brutality, sound=R.raw.one_more_time),
    RING_OF_SKULLS(R.drawable.card_skulls, getUsed={
        if (it.skeletonLocations <= 0) {
            throw kotlin.Exception("Nowhere for a new skeleton!")
        }
        it.skeletonLocations -= 1
    }),
    ROCKET_BOOTS(R.drawable.card_boots),
    SPIKED_SHIELD(R.drawable.card_spiked),
    SUPER_HEALING_POTION(R.drawable.card_healing, sound=R.raw.drinking, getUsed={
        if (it.hp >= it.maxHp) {
            throw kotlin.Exception("Already at max HP!")
        }
        it.hp = min(it.hp+7, it.maxHp)
    }),
    TOWER_SHIELD(R.drawable.card_tower),
}