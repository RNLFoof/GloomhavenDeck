package com.example.gloomhavendeck

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.intellij.lang.annotations.RegExp
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

@RequiresApi(Build.VERSION_CODES.N)
data class Enemy(var creationString: String) {
    // Defaults
    var taken: Int = 0
    var maxHp: Int = 0
    var name: String = ""
    var shield = 0
    var retaliate = 0
    var inRetaliateRange = false
    var inMeleeRange = false
    var inBallistaRange = false
    var targeted = false
    var extraTarget = false
    var poisoned = false
    var stunned = false
    var muddled = false
    // Parse
    init {
        val chunks = creationString.split(",")
        // HP and name
        val firstChunk = chunks.first().trim()
        val search = Regex("^(.+?)\\s+?(\\d+)\\s*?(\\d+)?\$").find(firstChunk)
        if (search != null) {
            name = search.groups[1]!!.value.trim()
            maxHp = search.groups[2]!!.value.toInt()
            if (search.groups[3] != null) {
                taken = search.groups[3]!!.value.toInt()
            }
        } else {
            throw Exception("Wasn't able to set name and Max HP using $firstChunk")
        }
        // Other stuff
        if (chunks.size > 1) {
            for (chunk in chunks.subList(1, chunks.size)) {
                val trimmedChunk = chunk.trim()
                var chunkSolved = false
                // Bools
                for (property in Enemy::class.memberProperties) {
                    if (property.returnType == Boolean::class.createType()
                        && trimmedChunk.lowercase() in property.name.lowercase()
                    ) {
                        (property as KMutableProperty<*>).setter.call(this, true)
                        chunkSolved = true
                        break
                    }
                }
                // Ints
                if (!chunkSolved) {
                    // Get both vars
                    var name: String? = null
                    var value: Int? = null
                    var intSearch = Regex("^([a-zA-Z]+).*?(\\d+)\$").find(trimmedChunk)
                    if (intSearch != null) {
                        name = intSearch.groups[1]!!.value
                        value = intSearch.groups[2]!!.value.toInt()
                    }
                    else {
                        intSearch = Regex("^(\\d+).*?([a-zA-Z]+)\$").find(trimmedChunk)
                        if (intSearch != null) {
                            name = intSearch.groups[2]!!.value
                            value = intSearch.groups[1]!!.value.toInt()
                        }
                    }
                    if (name != null && value != null) {
                        for (property in Enemy::class.memberProperties) {
                            if (property.returnType == Int::class.createType()
                                && name.lowercase() in property.name.lowercase()
                            ) {
                                (property as KMutableProperty<*>).setter.call(
                                    this, value
                                )
                                chunkSolved = true
                                break
                            }
                        }
                    }
                }
                // Did you win?
                if (!chunkSolved) {
                    throw Exception("Wasn't able to get anything from $trimmedChunk")
                }
            }
        }
        Log.d("hey", this.toString())
    }
    var dead = taken >= maxHp

    companion object {
        fun createMany(block: String) = sequence {
            // This previous name stuff lets you, for example, write
            // "Dog 1\n2\n3"
            // instead of
            // "Dog 1\nDog 2\nDog 3"
            val nameRegex = Regex("^[a-zA-Z]+")
            var previousName = nameRegex.find(block)!!.value
            for (line in block.split("\n")) {
                val currentName = nameRegex.find(line)
                if (currentName == null) {
                    yield(Enemy(previousName + line))
                }
                else {
                    previousName = currentName.value
                    yield(Enemy(line))
                }
            }
        }
    }

    fun getAttacked(card: Card, player: Player) {
        if (getTargetable()) {
            throw Exception("Shouldn't be attacking an untargetable guy.")
        }
        taken += Integer.max(0, card.value - effectiveShield(player)) + if (poisoned) 1 else 0
        dead = taken >= maxHp
        if (!dead && inRetaliateRange) {
            player.hp -= retaliate
        }
        if (card.stun) {
            stunned = true
        }
        if (card.muddle) {
            muddled = true
        }
    }

    fun effectiveShield(player: Player): Int {
        return Integer.max(0, shield - player.pierce)
    }

    fun getHp(): Int {
        return maxHp - taken
    }

    fun getTargetable(): Boolean {
        return !dead && targeted
    }

    override fun toString(): String {
        var ret = "$name $maxHp $taken"
        for (property in Enemy::class.memberProperties) {
            // Bools
            if (property.returnType == Boolean::class.createType()
                && property.getter.call(this) as Boolean) {
                ret += ", ${property.name}"
            }
            // Ints
            if (property.returnType == Int::class.createType()
                && property.getter.call(this) as Int != 0) {
                if (property.name != "maxHp" && property.name != "taken") {
                    ret += ", ${property.getter.call(this)} ${property.name}"
                }
            }
        }
        return ret
    }
}
