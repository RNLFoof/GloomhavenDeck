package com.example.gloomhavendeck
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.lang.Integer.min
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
enum class Item(val graphic: Int, val sound: SoundBundle=SoundBundle.DEFAULT,
                val deactivationSound: SoundBundle=SoundBundle.DEFAULT, val permanent: Boolean=false,
                val spendOnly: Boolean = false, val getsActivated: Boolean = false,
                val getUsed: (Controller, Boolean) -> Unit = fun (_: Controller, fullAutoBehavior: Boolean) {},
                val getDeactivated: (Controller, Boolean) -> Unit = fun (_: Controller, fullAutoBehavior: Boolean) {}
) {
    CLOAK_OF_PHASING(R.drawable.card_cloakofphasing, permanent = true),
    CLOAK_OF_POCKETS(R.drawable.card_cloak, permanent = true),
    LUCKY_EYE(R.drawable.card_luckyeye, sound=SoundBundle.STRENGTHEN, getUsed=fun (controller, _){
        controller.player?.updateStatus(Status.STRENGTHEN, 2)
    }),
    MAJOR_CURE_POTION(R.drawable.card_cure, sound=SoundBundle.DRINK, getUsed=fun (controller, viaPipis){
        if (controller.player != null) {
            if (!viaPipis && !controller.player!!.statuses.any { it.negative }) {
                throw ItemUnusableException("No negative statuses!")
            }
            controller.player!!.statuses.filter { it.negative }.forEach { controller.player!!.updateStatus(it, 0 )}
        }
    }),
    MAJOR_POWER_POTION(R.drawable.card_power, sound=SoundBundle.DRINK),
    MAJOR_STAMINA_POTION(R.drawable.card_majorstamina, sound=SoundBundle.DRINK, getUsed=fun (controller, _) {
        controller.player!!.discardedCards -= 3
    }),
    MINOR_STAMINA_POTION(R.drawable.card_minorstamina, sound=SoundBundle.DRINK, getUsed=fun (controller, _) {
        controller.player!!.discardedCards -= 2
    }),
    PENDANT_OF_DARK_PACTS(R.drawable.card_pendant, sound=SoundBundle.PENDANTOFDARKPACTS, getUsed=fun (controller, fullAutoBehavior) {
        if (fullAutoBehavior && controller.inventory != null) {
            if (controller.inventory!!.unusableItems.size <= 1
                || (controller.inventory!!.unusableItems.size <= 2 && controller.inventory!!.unusableItems.contains(
                    UTILITY_BELT
                ))
            ) {
                throw ItemUnautomatableException("There aren't two items to recover!")
            }
            controller.inventory!!.recover(howMany = 2, cantRecover = listOf(UTILITY_BELT))
        }
        controller.deck?.curse()
    }),
    RING_OF_BRUTALITY(R.drawable.card_brutality, sound=SoundBundle.RINGOFBRUTALITY),
    RING_OF_DUALITY(R.drawable.card_duality, sound=SoundBundle.RINGOFDUALITY),
    RING_OF_SKULLS(R.drawable.card_skulls, getsActivated = true, sound=SoundBundle.JOHNSON, deactivationSound=SoundBundle.DEATH),
    ROCKET_BOOTS(R.drawable.card_boots, sound=SoundBundle.JUMP, spendOnly = true),
    SECOND_CHANCE_RING(R.drawable.card_secondchancering, sound=SoundBundle.SECONDCHANCERING),
    SPIKED_SHIELD(R.drawable.card_spiked, spendOnly = true),
    SUPER_HEALING_POTION(R.drawable.card_healing, sound=SoundBundle.DRINK, getUsed=fun (controller, fullAutoBehavior) {
        controller.player?.heal(7, true)
    }),
    TOWER_SHIELD(R.drawable.card_tower, spendOnly = true),
    UTILITY_BELT(R.drawable.card_belt, sound=SoundBundle.UTILITYBELT, getUsed=fun(controller, fullAutoBehavior) {
        controller.inventory?.let {
            if (fullAutoBehavior) {
                if (it.unusableItems.size <= 0) {
                    throw ItemUnautomatableException("There isn't an item to recover!")
                }
                it.recover(howMany = 1)
            }
        }
    }),
    WALL_SHIELD(R.drawable.card_wallshield, sound=SoundBundle.SHIELD, spendOnly = true),
    WAR_HAMMER(R.drawable.card_warhammer, sound=SoundBundle.WARHAMMER);

    @SuppressLint("UseCompatLoadingForDrawables")
    // Activated and used would probably be better as an enum but I suck
    fun getImageView(context: Context, used: Boolean, activated: Boolean): ImageView {
        val imageView = ImageView(context)
        if (activated) {
            imageView.setImageResource(graphic)
            imageView.setColorFilter(Color.GREEN)
        } else if (used) {
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

class ItemUnusableException(override val message: String?): Exception(message)
class ItemUnautomatableException(override val message: String?): Exception(message)