package com.example.gloomhavendeck

enum class Status(val icon: String){
    DISARM("\uD83D\uDC4B"),
    IMMOBILE("\uD83E\uDD7E"),
    MUDDLE("❓"),
    POISON("☠"),
    REGENERATION("❤"),
    STRENGTHEN("\uD83D\uDCAA"),
    STUN("💥"),
    WOUND("\uD83E\uDE78");

    override fun toString(): String {
        return icon
    }
}