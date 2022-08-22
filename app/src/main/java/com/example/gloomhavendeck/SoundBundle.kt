package com.example.gloomhavendeck

import java.util.*
import kotlin.collections.HashMap


data class SoundBundle(var weights: LinkedHashMap<Any, Float>) {
    companion object{
        val PLUS1_GUITAR = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.plus1_guitar_guitar_impact1 to 1f,
                    R.raw.plus1_guitar_guitar_impact2 to 1f,
                )))
        val PLUS1 = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.plus1_bat_hit to 1f,
                    R.raw.plus1_boom to 1f,
                    R.raw.plus1_et_cagebreak to 1f,
                    R.raw.plus1_hitsound to 1f,
                    R.raw.plus1_pan to 1f,
                    R.raw.plus1_thwomp to 1f,
                    PLUS1_GUITAR to 0.5f,
                )))
        val PLUS2 = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.plus2_ttyd_jesus to 1f,
                    R.raw.plus2_ttyd_layered_explosion to 1f,
                )))
        val MINUS1 = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.minus1_et_playerdie to 1f,
                    R.raw.minus1_fnf_death to 1f,
                    R.raw.minus1_hamsterball_glue_stuck to 1f,
                )))
        val MINUS2 = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.minus2_mario_fall to 1f,
                )))
        val BLESS = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.bless_cheer to 1f,
                    R.raw.bless_happy_birthday to 0.1f,
                    R.raw.bless_tada to 1f,
                )))
        val CURSE = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.curse_ttyd_ghost to 1f,
                    R.raw.curse_ttyd_pig to 1f,
                )))
        val DEATH_AUDIES_XP = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.death_audies_xp_windows2kgoodbye to 1f,
                    R.raw.death_audies_xp_xpgoodbye to 1f,
                )))
        val DEATH_AUDIES = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.death_audies_cartoonfall to 1f,
                    R.raw.death_audies_foghorn to 1f,
                    R.raw.death_audies_screamman to 1f,
                    DEATH_AUDIES_XP to 1f,
                )))
        val DEATH_BEETHRO = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.death_beethro_beethrodie1 to 1f,
                    R.raw.death_beethro_beethrodie2 to 1f,
                )))
        val DEATH_GRINDER = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.death_grinder_grinderhuman_01 to 1f,
                    R.raw.death_grinder_grinderhuman_02 to 1f,
                )))
        val DEATH = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.death_die to 1f,
                    R.raw.death_et_jetdie to 1f,
                    R.raw.death_medic_mvm_class_is_dead03 to 1f,
                    R.raw.death_mvm_player_died to 1f,
                    R.raw.death_poof to 1f,
                    R.raw.death_ttyd_mario_fucking_dies to 1f,
                    DEATH_AUDIES to 1f,
                    DEATH_BEETHRO to 1f,
                    DEATH_GRINDER to 1f,
                )))
        val DEFAULT_STONE = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.default_stone_stone1 to 1f,
                    R.raw.default_stone_stone2 to 1f,
                    R.raw.default_stone_stone3 to 1f,
                    R.raw.default_stone_stone4 to 1f,
                )))
        val DEFAULT = SoundBundle(
            LinkedHashMap(
                mapOf(
                    DEFAULT_STONE to 1f,
                )))
        val DISADVANTAGE = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.disadvantage_windows_xp_error to 1f,
                )))
        val DISCARD = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.discard_banana_slip to 1f,
                    R.raw.discard_sneeze to 1f,
                )))
        val DRINK_AUDIES = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.drink_audies_drain to 1f,
                    R.raw.drink_audies_pissmiss to 1f,
                )))
        val DRINK = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.drink_bottle_break to 0.1f,
                    R.raw.drink_drinking to 1f,
                    R.raw.drink_et_istolethis to 1f,
                    DRINK_AUDIES to 1f,
                )))
        val EXTRATARGET = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.extratarget_gun to 1f,
                    R.raw.extratarget_hamsterball_catapult to 1f,
                    R.raw.extratarget_sentry_spot_client to 1f,
                )))
        val JOHNSON = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.johnson_johnson to 1f,
                )))
        val JUMP_DEMOCHARGE = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.jump_democharge_demo_charge_windup1 to 1f,
                    R.raw.jump_democharge_demo_charge_windup2 to 1f,
                    R.raw.jump_democharge_demo_charge_windup3 to 1f,
                )))
        val JUMP_TIMROCKETS = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.jump_timrockets_rocket_launch_also to 1f,
                    R.raw.jump_timrockets_rocket_launch to 1f,
                )))
        val JUMP = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.jump_bottlerocket to 1f,
                    R.raw.jump_et_balloondeflating to 0.05f,
                    R.raw.jump_et_flyingwhee to 0.05f,
                    R.raw.jump_et_missilefire to 1f,
                    R.raw.jump_et_springuse to 1f,
                    R.raw.jump_jack_in_the_box to 1f,
                    R.raw.jump_launch1 to 1f,
                    JUMP_DEMOCHARGE to 1f,
                    JUMP_TIMROCKETS to 1f,
                )))
        val MUDDLE = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.muddle_hamsterball_dizzy to 1f,
                )))
        val NULL = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.null_buzzer to 1f,
                )))
        val PENDANTOFDARKPACTS = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.pendantofdarkpacts_loud_bird to 1f,
                )))
        val PIERCE = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.pierce_shield_break to 1f,
                )))
        val REFRESH = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.refresh_chest_open to 1f,
                    R.raw.refresh_h553 to 1f,
                )))
        val RINGOFBRUTALITY = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.ringofbrutality_one_more_time to 1f,
                )))
        val SHIELD_IRONDOOR = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.shield_irondoor_close1 to 1f,
                    R.raw.shield_irondoor_close2 to 1f,
                    R.raw.shield_irondoor_close3 to 1f,
                    R.raw.shield_irondoor_close4 to 1f,
                )))
        val SHIELD_IRONGOLEM = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.shield_irongolem_hit1 to 1f,
                    R.raw.shield_irongolem_hit2 to 1f,
                    R.raw.shield_irongolem_hit3 to 1f,
                    R.raw.shield_irongolem_hit4 to 1f,
                )))
        val SHIELD_MCSHIELD = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.shield_mcshield_block1 to 1f,
                    R.raw.shield_mcshield_block2 to 1f,
                    R.raw.shield_mcshield_block3 to 1f,
                    R.raw.shield_mcshield_block4 to 1f,
                    R.raw.shield_mcshield_block5 to 1f,
                )))
        val SHIELD_WOODDOOR = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.shield_wooddoor_close to 1f,
                    R.raw.shield_wooddoor_close2 to 1f,
                    R.raw.shield_wooddoor_close3 to 1f,
                    R.raw.shield_wooddoor_close4 to 1f,
                    R.raw.shield_wooddoor_close5 to 1f,
                    R.raw.shield_wooddoor_close6 to 1f,
                )))
        val SHIELD = SoundBundle(
            LinkedHashMap(
                mapOf(
                    SHIELD_IRONDOOR to 1f,
                    SHIELD_IRONGOLEM to 1f,
                    SHIELD_MCSHIELD to 1f,
                    SHIELD_WOODDOOR to 0.1f,
                )))
        val SHUFFLE = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.shuffle_shuffle to 1f,
                )))
        val STRENGTHEN = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.strengthen_ringside to 1f,
                )))
        val STUN = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.stun_ttyd_dizzy_dial to 1f,
                    R.raw.stun_ttyd_sleep to 1f,
                    R.raw.stun_ttyd_timestop to 1f,
                )))
        val SUMMON = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.summon_roar to 1f,
                )))
        val UTILITYBELT = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.utilitybelt_metal_falling to 1f,
                )))
        val X2 = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.x2_blast_zone to 1f,
                )))

    }

    constructor(): this(LinkedHashMap())

    constructor(i: Iterable<Any>) : this() {
        i.forEach { weights[it] = 1f }
    }

    constructor(i: Int) : this(listOf(i)) {
    }

    fun getSound(): Any {
        if (weights.size == 0) {
            throw Exception("No freaking sounds in this bundle")
        }

        val totalWeight = weights.values.sum()
        var countdown = Math.random() * totalWeight
        var countup = 0
        while (true) {
            val currentSound = weights.keys.toList()[countup]
            val currentWeight = weights[currentSound]!!
            countdown -= currentWeight
            if (countdown <= 0) {
                try {
                    return (currentSound as SoundBundle).getSound()
                } catch(e : Exception) {
                    return currentSound
                }
            }
            countup++
        }
    }
}