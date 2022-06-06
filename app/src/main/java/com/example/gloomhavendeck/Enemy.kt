package com.example.gloomhavendeck

import android.util.Log
import org.intellij.lang.annotations.RegExp
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

data class Enemy(var creationString: String) {
    // Defaults
    var hp: Int = 0
    var name: String = ""
    var shield = 0
    var retaliate = 0
    var inMeleeRange = false
    var inBallistaRange = false
    var extraTarget = false
    var poisoned = false
    // Parse
    init {
        val chunks = creationString.split(",")
        // HP and name
        val firstChunk = chunks.first().trim()
        val search = Regex("^(.+?)(\\d+)\$").find(firstChunk)
        if (search != null) {
            name = search.groups[1]!!.value.trim()
            hp = search.groups[2]!!.value.toInt()
        } else {
            throw Exception("Wasn't able to set name and HP using $firstChunk")
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
    var dead = hp <= 0

    companion object {
        fun createMany(block: String) = sequence {
            // This previous name stuff lets you, for example, write
            // "Dog 1\n2\n3"
            // instead of
            // "Dog 1\nDog 2\nDog 3"
            val nameRegex = Regex("^[a-zA-Z]+")
            var previousName = nameRegex.find(block)!!.value
            for (line in block.split("\n")) {
                Log.d("hey pname", previousName)
                val currentName = nameRegex.find(line)
                if (currentName == null) {
                    yield(Enemy(previousName + " " + line))
                }
                else {
                    previousName = currentName.value
                    yield(Enemy(line))
                }
            }
        }
    }

    fun getAttacked(card: Card, player: Player) {
        if (dead) {
            throw Exception("Shouldn't be attacking a dead guy.")
        }
        val effectiveShield = Integer.max(0, shield - player.pierce)
        hp -= Integer.max(0, card.value - effectiveShield) + if (poisoned) 1 else 0
        dead = hp <= 0
        if (dead && inMeleeRange) {
            player.skeletonLocations += 1
        }
    }

    override fun toString(): String {
        var ret = "$name $hp"
        for (property in Enemy::class.memberProperties) {
            // Bools
            if (property.returnType == Boolean::class.createType()
                && property.getter.call(this) as Boolean) {
                ret += ", ${property.name}"
            }
            // Ints
            if (property.returnType == Int::class.createType()
                && property.getter.call(this) as Int != 0) {
                if (property.name != "hp") {
                    ret += ", ${property.getter.call(this)} ${property.name}"
                }
            }
        }
        return ret
    }
}
