package com.example.gloomhavendeck

enum class Status(val icon: String, val negative: Boolean=false){
    DISARM("\uD83D\uDC4B", negative = true),
    IMMOBILE("\uD83E\uDD7E", negative = true),
    MUDDLE("❓", negative = true),
    POISON("☠", negative = true),
    REGENERATION("❤"),
    STRENGTHEN("\uD83D\uDCAA"),
    STUN("💥", negative = true),
    WOUND("\uD83E\uDE78", negative = true);

    override fun toString(): String {
        return icon
    }
}