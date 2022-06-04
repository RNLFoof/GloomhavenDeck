package com.example.gloomhavendeck

data class Card(
    var value: Int = 0, var multiplier: Boolean = false,
    var flippy: Boolean = false, var spinny: Boolean = false,
    var lose: Boolean = false, var stun: Boolean = false, var muddle: Boolean = false,
    var refresh: Boolean = false, var pierce: Int = 0, var extraTarget: Boolean = false) {


    override fun toString(): String {
        Card() + Card()
        // Named stuff
        if (lose) {
            if (value == 2) {
                return " [bless] "
            }
            return " [curse] "
        }
        if (value == 0 && multiplier) {
            return " [null] "
        }

        var ret = ""
        ret += " ["

        if (stun) {ret += "💥"}
        if (muddle) {ret += "❓"}
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
        if (value == 0 && multiplier) {
            return -99
        }
        return value * 2 + (
            if (pierce > 0 || stun || muddle || extraTarget || refresh) 1 else 0
        )
    }

    operator fun plus(increment: Card): Card {
        val retCard = this.copy()
        retCard.flippy = false;
        if (increment.multiplier)
            retCard.value *= increment.value
        else
            retCard.value += increment.value
        retCard.pierce += increment.pierce
        // retCard.flippy = retCard.flippy || card.flippy
        retCard.spinny = retCard.spinny || increment.spinny
        retCard.lose = retCard.lose || increment.lose
        retCard.stun = retCard.stun || increment.stun
        retCard.muddle = retCard.muddle || increment.muddle
        retCard.extraTarget = retCard.extraTarget || increment.extraTarget
        retCard.refresh = retCard.refresh || increment.refresh

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