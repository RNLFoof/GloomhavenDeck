package com.example.gloomhavendeck
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Integer.min

@RequiresApi(Build.VERSION_CODES.N)
enum class Item(val graphic: Int, val sound: Int=R.raw.stone2, val permanent: Boolean=false,
    val getUsed: (Player, Deck) -> Unit = fun (_: Player, _: Deck) {}) {
    CLOAK_OF_POCKETS(R.drawable.card_cloak, permanent = true),
    MAJOR_CURE_POTION(R.drawable.card_cure, sound=R.raw.drinking, getUsed=fun (player, _){
        if (!player.statuses.any{it.negative}) {
            throw kotlin.Exception("No negative statuses!")
        }
        player.statuses.removeIf{it.negative}
    }),
    MAJOR_POWER_POTION(R.drawable.card_power, sound=R.raw.drinking),
    MAJOR_STAMINA_POTION(R.drawable.card_majorstamina, sound=R.raw.drinking),
    MINOR_STAMINA_POTION(R.drawable.card_minorstamina, sound=R.raw.drinking),
    PENDANT_OF_DARK_PACTS(R.drawable.card_pendant, getUsed=fun (player, deck) {
        if (player.inventory.unusableItems.size <= 1) {
            throw kotlin.Exception("There aren't two items to recover!")
        }
        player.inventory.recover(player, deck, howMany = 2)
        deck.curse()
    }),
    RING_OF_BRUTALITY(R.drawable.card_brutality, sound=R.raw.one_more_time),
    RING_OF_SKULLS(R.drawable.card_skulls),
    ROCKET_BOOTS(R.drawable.card_boots),
    SPIKED_SHIELD(R.drawable.card_spiked),
    SUPER_HEALING_POTION(R.drawable.card_healing, sound=R.raw.drinking, getUsed=fun (player, _){
        if (player.hp >= player.maxHp) {
            throw kotlin.Exception("Already at max HP!")
        }
        player.hp = min(player.hp+7, player.maxHp)
    }),
    TOWER_SHIELD(R.drawable.card_tower),
}