package com.example.gloomhavendeck
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.lang.Integer.min
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
enum class Item(val graphic: Int, val sound: Int=R.raw.stone2, val permanent: Boolean=false,
                val spendOnly: Boolean = false,
                val getUsed: (Player, Deck) -> Unit = fun (_: Player, _: Deck) {}) {
    CLOAK_OF_POCKETS(R.drawable.card_cloak, permanent = true),
    LUCKY_EYE(R.drawable.card_luckyeye, sound=R.raw.ringside, getUsed=fun (player, _){
        if (!player.statuses.contains(Status.STRENGTHEN)) {
            player.statuses.add(Status.STRENGTHEN)
        }
    }),
    MAJOR_CURE_POTION(R.drawable.card_cure, sound=R.raw.drinking, getUsed=fun (player, _){
        if (!player.statuses.any{it.negative}) {
            throw kotlin.Exception("No negative statuses!")
        }
        player.statuses.removeIf{it.negative}
    }),
    MAJOR_POWER_POTION(R.drawable.card_power, sound=R.raw.drinking),
    MAJOR_STAMINA_POTION(R.drawable.card_majorstamina, sound=R.raw.drinking, getUsed=fun (player, _) {
        player.discardedCards -= 3
    }),
    MINOR_STAMINA_POTION(R.drawable.card_minorstamina, sound=R.raw.drinking, getUsed=fun (player, _) {
        player.discardedCards -= 2
    }),
    PENDANT_OF_DARK_PACTS(R.drawable.card_pendant, sound=R.raw.loud_bird, getUsed=fun (player, deck) {
        if (player.inventory.unusableItems.size <= 1
            || (player.inventory.unusableItems.size <= 2 && player.inventory.unusableItems.contains(UTILITY_BELT))
        ) {
            throw kotlin.Exception("There aren't two items to recover!")
        }
        player.inventory.recover(player, deck, howMany = 2, cantRecover = listOf(UTILITY_BELT))
        deck.curse()
    }),
    RING_OF_BRUTALITY(R.drawable.card_brutality, sound=R.raw.one_more_time),
    RING_OF_SKULLS(R.drawable.card_skulls, sound=R.raw.poof),
    ROCKET_BOOTS(R.drawable.card_boots, spendOnly = true),
    SPIKED_SHIELD(R.drawable.card_spiked, spendOnly = true),
    SUPER_HEALING_POTION(R.drawable.card_healing, sound=R.raw.drinking, getUsed=fun (player, _){
        if (player.hp >= player.maxHp) {
            throw kotlin.Exception("Already at max HP!")
        }
        player.hp = min(player.hp+7, player.maxHp)
    }),
    TOWER_SHIELD(R.drawable.card_tower, spendOnly = true),
    UTILITY_BELT(R.drawable.card_belt, sound=R.raw.metal_falling, getUsed=fun(player, deck) {
        if (player.inventory.unusableItems.size <= 0) {
            throw kotlin.Exception("There isn't an item to recover!")
        }
        player.inventory.recover(player, deck, howMany = 1)
    }),
    WALL_SHIELD(R.drawable.card_wallshield, sound=R.raw.close_iron_door, spendOnly = true);

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getImageView(context: Context, used: Boolean): ImageView {
        val imageView = ImageView(context)
        if (used) {
            imageView.setImageResource(graphic)
        } else if (spendOnly) {
            imageView.setImageResource(graphic)
            imageView.rotation = 90f;
        } else {
            imageView.setImageResource(graphic)
            imageView.foreground = context.getDrawable(R.drawable.card_transuseditem)
        }
        imageView.rotation += (Random().nextFloat()*1-0.5).toFloat() // This masks bad scanning lol
        imageView.adjustViewBounds = true
        return imageView
    }
}