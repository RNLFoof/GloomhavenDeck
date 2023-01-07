package com.example.gloomhavendeck

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    var value: Int = 0,
    var multiplier: Boolean = false,
    var nullOrCurse: Boolean = false, // Would be null but that's reserved
    var flippy: Boolean = false,
    var spinny: Boolean = false,
    var lose: Boolean = false,
    var stun: Boolean = false,
    var muddle: Boolean = false,
    var refresh: Boolean = false,
    var pierce: Int = 0,
    var extraTarget: Boolean = false,
    val healAlly: Int = 0,
    val shieldSelf: Int = 0,
    val element: Element? = null,
    val regenerate: Boolean = false,
    val curses: Boolean = false,
) {


    override fun toString(): String {
        Card(healAlly = 2) + Card(healAlly = 2)
        // Named stuff
        if (lose && value == 2) {
            return " [bless] "
        }
        if (lose && value == 0) {
            return " [curse] "
        }
        if (value == 0 && nullOrCurse) {
            return " [null] "
        }

        var ret = ""
        ret += " ["

        if (stun) {ret += Status.STUN.icon}
        if (muddle) {ret += Status.MUDDLE.icon}
        if (refresh){ret += "\uD83D\uDC5C"}
        if (extraTarget) {ret += "\uD83C\uDFAF"}
        if (pierce > 0) {ret += "$pierce➺"}
        if (ret == " [" || value != 0 || multiplier) {
            ret += (if (multiplier) "x" else if (value >= 0) "+" else "") +
            value.toString()
        }

        if (flippy) {
            ret += "⤳"
        } else if (spinny) {
            ret += "⟳"
        }

        ret += "] "
        return ret
    }

    // Uhhhh
    fun toInt(): Int {
        if (nullOrCurse) {
            return -99
        }
        return value * 2 + (
            if (pierce > 0 || stun || muddle || extraTarget || refresh) 1 else 0
        )
    }

    operator fun plus(increment: Card): Card {
        val retCard = this.copy()
        retCard.flippy = false // Since all the cards combine, this becomes irrelevant

        // The few properties that still apply if this is a null/curse
        retCard.spinny = retCard.spinny || increment.spinny
        retCard.nullOrCurse = retCard.nullOrCurse || increment.nullOrCurse

        // If it's a null/curse, end here, nothing else matters
        if (retCard.nullOrCurse) {
            retCard.value = 0
            return retCard
        }

        // and the rest of it
        if (increment.multiplier)
            retCard.value *= increment.value
        else
            retCard.value += increment.value
        retCard.pierce += increment.pierce
        retCard.stun = retCard.stun || increment.stun
        retCard.muddle = retCard.muddle || increment.muddle
        retCard.extraTarget = retCard.extraTarget || increment.extraTarget
        retCard.refresh = retCard.refresh || increment.refresh
        retCard.lose = retCard.lose || increment.lose // I'm not actually sure this needs to be here?

        return retCard
    }

    operator fun compareTo(comparison: Card): Int {
        return toInt() - comparison.toInt()
    }
}

fun List<Card>.sum(): Card {
    if (this.size == 1) {
        return this[0];
    }
    var retCard = this[0];
    for (card in this.subList(1, this.size)) {
        retCard += card;
    }
    return retCard;
}