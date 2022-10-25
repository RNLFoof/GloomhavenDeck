package com.example.gloomhavendeck
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.lang.Integer.min
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
enum class Item(val graphic: Int, val sound: SoundBundle=SoundBundle.DEFAULT, val permanent: Boolean=false,
                val spendOnly: Boolean = false,
                val getUsed: (Player, Deck, Boolean) -> Unit = fun (_: Player, _: Deck, fullAutoBehavior: Boolean) {}) {
    CLOAK_OF_POCKETS(R.drawable.card_cloak, permanent = true),
    CLOAK_OF_PHASING(R.drawable.card_cloakofphasing, permanent = true),
    LUCKY_EYE(R.drawable.card_luckyeye, sound=SoundBundle.STRENGTHEN, getUsed=fun (player, _, _){
        if (!player.statuses.contains(Status.STRENGTHEN)) {
            player.statusDict[Status.STRENGTHEN] = 2
        }
    }),
    MAJOR_CURE_POTION(R.drawable.card_cure, sound=SoundBundle.DRINK, getUsed=fun (player, _, viaPipis){
        if (!viaPipis && !player.statuses.any{it.negative}) {
            throw kotlin.Exception("No negative statuses!")
        }
        player.statusDict.filterKeys { it.negative }.forEach{ player.statusDict[it.key] = 0}
    }),
    MAJOR_POWER_POTION(R.drawable.card_power, sound=SoundBundle.DRINK),
    MAJOR_STAMINA_POTION(R.drawable.card_majorstamina, sound=SoundBundle.DRINK, getUsed=fun (player, _, _) {
        player.discardedCards -= 3
    }),
    MINOR_STAMINA_POTION(R.drawable.card_minorstamina, sound=SoundBundle.DRINK, getUsed=fun (player, _, _) {
        player.discardedCards -= 2
    }),
    PENDANT_OF_DARK_PACTS(R.drawable.card_pendant, sound=SoundBundle.PENDANTOFDARKPACTS, getUsed=fun (player, deck, fullAutoBehavior) {
        if (fullAutoBehavior) {
            if (player.inventory.unusableItems.size <= 1
                || (player.inventory.unusableItems.size <= 2 && player.inventory.unusableItems.contains(
                    UTILITY_BELT
                ))
            ) {
                throw kotlin.Exception("There aren't two items to recover!")
            }
            player.inventory.recover(player, deck, howMany = 2, cantRecover = listOf(UTILITY_BELT))
        }
        deck.curse()
    }),
    RING_OF_BRUTALITY(R.drawable.card_brutality, sound=SoundBundle.RINGOFBRUTALITY),
    RING_OF_SKULLS(R.drawable.card_skulls, sound=SoundBundle.DEATH),
    RING_OF_DUALITY(R.drawable.card_duality, sound=SoundBundle.RINGOFDUALITY),
    ROCKET_BOOTS(R.drawable.card_boots, sound=SoundBundle.JUMP, spendOnly = true),
    SPIKED_SHIELD(R.drawable.card_spiked, spendOnly = true),
    SUPER_HEALING_POTION(R.drawable.card_healing, sound=SoundBundle.DRINK, getUsed=fun (player, _, fullAutoBehavior) {
        if (player.statuses.contains(Status.WOUND)) {
            player.statusDict[Status.WOUND] = 0
        }
        if (player.statuses.contains(Status.POISON)) {
            player.statusDict[Status.POISON] = 0
        }
        else {
            if (player.hp >= player.maxHp) {
                throw kotlin.Exception("Already at max HP!")
            }
            player.hp = min(player.hp + 7, player.maxHp)
        }
    }),
    TOWER_SHIELD(R.drawable.card_tower, spendOnly = true),
    UTILITY_BELT(R.drawable.card_belt, sound=SoundBundle.UTILITYBELT, getUsed=fun(player, deck, fullAutoBehavior) {
        if (fullAutoBehavior) {
            if (player.inventory.unusableItems.size <= 0) {
                throw kotlin.Exception("There isn't an item to recover!")
            }
            player.inventory.recover(player, deck, howMany = 1)
        }
    }),
    WALL_SHIELD(R.drawable.card_wallshield, sound=SoundBundle.SHIELD, spendOnly = true),
    WAR_HAMMER(R.drawable.card_warhammer, sound=SoundBundle.WARHAMMER, spendOnly = true);

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getImageView(context: Context, used: Boolean): ImageView {
        val imageView = ImageView(context)
        if (used) {
            imageView.setImageResource(graphic)
        } else if (spendOnly) {
            imageView.setImageResource(graphic)
            imageView.rotation = 90f
        } else {
            imageView.setImageResource(graphic)
            imageView.foreground = context.getDrawable(R.drawable.card_transuseditem)
        }
        imageView.rotation += (Random().nextFloat()*1-0.5).toFloat() // This masks bad scanning lol
        imageView.adjustViewBounds = true
        return imageView
    }
}