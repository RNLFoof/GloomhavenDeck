package com.example.gloomhavendeck

import java.util.*
import kotlin.collections.HashMap


data class SoundBundle(var weights: LinkedHashMap<Any, Float>) {
    companion object{
        val DEFAULT = SoundBundle(R.raw.stone2)
        val DRINKING = SoundBundle(
            LinkedHashMap(
                mapOf(
                    R.raw.drinking to 1f,
                    R.raw.pissmiss to 0.1f,
                    R.raw.drain to 0.1f,
        )))
        val BEETHRO = SoundBundle(listOf(
            R.raw.beethrodie1,
            R.raw.beethrodie2,
        ))
        val DEATH = SoundBundle(listOf(
                    R.raw.die,
                    R.raw.foghorn,
                    R.raw.screamman,
                    BEETHRO,
        ))
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