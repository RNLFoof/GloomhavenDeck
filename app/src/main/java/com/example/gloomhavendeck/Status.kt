package com.example.gloomhavendeck

enum class Status(val icon: String, val negative: Boolean=false, val roundBased: Boolean=true){
    DISARM("\uD83D\uDC4B", negative = true),
    IMMOBILE("\uD83E\uDD7E", negative = true),
    MUDDLE("❓", negative = true),
    POISON("☠", negative = true, roundBased = false),
    REGENERATION("❤", roundBased = false),
    STRENGTHEN("\uD83D\uDCAA"),
    STUN("💥", negative = true),
    WOUND("\uD83E\uDE78", negative = true, roundBased = false);

    fun getNextManualPosition(currentPosition: Int): Int {
        if (roundBased) {
            return (currentPosition + 2) % 3
        } else {
            return 1 - currentPosition
        }
    }

    override fun toString(): String {
        return icon
    }
}