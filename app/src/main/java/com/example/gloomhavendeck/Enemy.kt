package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.meta.Crap.Companion.getResourceAsText
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.math.pow

@RequiresApi(Build.VERSION_CODES.N)
@Serializable
data class Enemy(var creationString: String) {
    // Defaults
    var taken: Int = 0
    var maxHp: Int = 0
    var name: String = ""
    var shield = 0
    var retaliate = 0
    var attackersGainDisadvantage = false
    var inRetaliateRange = false
    var inMeleeRange = false
    var inBallistaRange = false
    var targeted = false
    var extraTarget = false
    var poisoned = false
    var stunned = false
    var muddled = false
    val dead: Boolean
        get() {return taken >= maxHp}
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
                        && property is KMutableProperty<*>
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
    }

    companion object {
        fun createMany(block: String, scenarioLevel: Int) = sequence {
            // The format "name: numbers" lets you pull from monster stats to quickly establish
            // Ex: vermling scout 7: 1 2 3 e5 6

            val monsterStatsString = getResourceAsText("res/raw/monster_stats.json")
            val monsterStatsJson: Map<String, JsonElement> = Json.parseToJsonElement(
                monsterStatsString
            ).jsonObject
            var jsonExpandedBlock = ""

            val fromJsonRegex = Regex("^([a-zA-Z ]+)(\\d)?[:;]([0-9 ]+)?([nN][0-9 ]+)?$")
            val intRegex = Regex("\\d+")
            val shieldRegex = Regex("Shield (\\d+)")
            val retaliateRegex = Regex("Retaliate (\\d+)")

            for (line in block.split("\n")) {
                if (line.strip().isEmpty()) {
                    continue
                }
                // Does this line contain a DB shorthand?
                val fromJsonMatch = fromJsonRegex.find(line)
                if (fromJsonMatch != null) {
                    // Set up the regex that will be used to find the monster by name
                    val monsterSearchName = fromJsonMatch.groups[1]!!.value
                    val monsterNameRegex = Regex(monsterSearchName.replace(" ", ".*?"), option=RegexOption.IGNORE_CASE)
                    // Assume the standard level unless otherwise specified
                    val monsterLevel = if (fromJsonMatch.groups[2] == null) scenarioLevel else fromJsonMatch.groups[2]!!.value.toInt()
                    // Get the numbers for normals/elites
                    val monsterEliteNumbers =
                        if (fromJsonMatch.groups[3] == null)
                        emptySequence()
                        else intRegex.findAll(fromJsonMatch.groups[3]!!.value).map{it.value.toInt()}
                    val monsterNormalNumbers =
                        if (fromJsonMatch.groups[4] == null)
                            emptySequence()
                        else intRegex.findAll(fromJsonMatch.groups[4]!!.value).map{it.value.toInt()}
                    // Try to find monster matches!
                    val matchedMonsters = mutableListOf<String>()
                    for (monsterKV in monsterStatsJson["monsters"]!!.jsonObject) {
                        if (monsterNameRegex.find(monsterKV.key) != null) {
                            // Found one! Keep track and get some attributes
                            matchedMonsters.add(monsterKV.key)
                            // The name is the first 3 characters of each word
                            val monsterName = compactedString(monsterKV.key)
                            val monster = monsterKV.value.jsonObject["level"]!!.jsonArray[monsterLevel]
                            val normalMonster = monster.jsonObject["normal"]!!.jsonObject
                            val eliteMonster = monster.jsonObject["elite"]!!.jsonObject

                            // Add elites
                            for (number in monsterEliteNumbers) {
                                val maxHP = eliteMonster["health"]!!.jsonPrimitive
                                val attributes = eliteMonster["attributes"]!!.jsonArray.joinToString("\n")
                                val shieldRegexMatch = shieldRegex.find(attributes)
                                val shield = if (shieldRegexMatch == null) 0 else shieldRegexMatch.groups[1]!!.value.toInt()
                                val retaliateRegexMatch = retaliateRegex.find(attributes)
                                val retaliate = if (retaliateRegexMatch == null) 0 else retaliateRegexMatch.groups[1]!!.value.toInt()
                                val attackersGainDisadvantage = attributes.contains("Attackers gain Disadvantage")
                                jsonExpandedBlock += "\n${monsterName}${number}e $maxHP 0, shield $shield, retaliate $retaliate${if (attackersGainDisadvantage) ", attackersGainDisadvantage" else ""}"
                            }

                            // Add normals
                            for (number in monsterNormalNumbers) {
                                val maxHP = normalMonster["health"]!!.jsonPrimitive
                                val attributes = normalMonster["attributes"]!!.jsonArray.joinToString("\n")
                                val shieldRegexMatch = shieldRegex.find(attributes)
                                val shield = if (shieldRegexMatch == null) 0 else shieldRegexMatch.groups[1]!!.value.toInt()
                                val retaliateRegexMatch = retaliateRegex.find(attributes)
                                val retaliate = if (retaliateRegexMatch == null) 0 else retaliateRegexMatch.groups[1]!!.value.toInt()
                                val attackersGainDisadvantage = attributes.contains("Attackers gain Disadvantage")
                                jsonExpandedBlock += "\n${monsterName}${number}n $maxHP 0, shield $shield, retaliate $retaliate${if (attackersGainDisadvantage) ", attackersGainDisadvantage" else ""}"
                            }
                        }
                    }
                    // Throw errors if you don't find exactly one
                    if (matchedMonsters.size == 0) {
                        throw Exception("Found no matches for $monsterSearchName")
                    }
                    if (matchedMonsters.size >= 2) {
                        throw Exception("Found multiple matches for $monsterSearchName: $matchedMonsters")
                    }
                }
                // If not, just put it back as is
                else {
                    jsonExpandedBlock += "\n$line"
                }
            }
            jsonExpandedBlock = jsonExpandedBlock.trim()

            // This previous name stuff lets you, for example, write
            // "Dog 1\n2\n3"
            // instead of
            // "Dog 1\nDog 2\nDog 3"
            val nameRegex = Regex("^[a-zA-Z]+")
            val previousNameMatch = nameRegex.find(jsonExpandedBlock)
            if (previousNameMatch != null) {
                var previousName = previousNameMatch.value
                for (line in jsonExpandedBlock.split("\n")) {
                    val currentName = nameRegex.find(line)
                    if (currentName == null) {
                        yield(Enemy(previousName + line))
                    } else {
                        previousName = currentName.value
                        yield(Enemy(line))
                    }
                }
            }
        }

        fun createOne(line: String, scenarioLevel: Int): Enemy {
            if ("\n" in line){
                throw Exception("ONE!!!!!!")
            }
            for (enemy in createMany(line, scenarioLevel)) {
                return enemy
            }
            throw Exception("I guess that wasn't valid.")
        }

        fun oneOfEach(): Sequence<Enemy> {
            // Mainly for testing
            var template = ""

            val monsterStatsString = getResourceAsText("res/raw/monster_stats.json")
            val monsterStatsJson: Map<String, JsonElement> = Json.parseToJsonElement(
                monsterStatsString
            ).jsonObject
            for (monsterKV in monsterStatsJson["monsters"]!!.jsonObject) {
                template += "${monsterKV.key}:1n2\n"
            }

            return createMany(template, 7)
        }

        fun oneOfEachInterestingGuy() = sequence {
            val interestingTests: MutableList<(Enemy) -> Boolean> = mutableListOf(
                { true },
                { enemy -> enemy.shield != 0 },
                { enemy -> enemy.retaliate != 0 },
                { enemy -> enemy.attackersGainDisadvantage },
                { enemy -> enemy.maxHp >= 5 },
                { enemy -> enemy.maxHp >= 10 },
                { enemy -> enemy.maxHp >= 20 },
            )

            for (enemy in oneOfEach()) {
                for ((n, test) in interestingTests.withIndex()) {
                    if (test(enemy)) {
                        yield(enemy)
                        interestingTests.removeAt(n)
                        break
                    }
                }
            }
        }

        fun teamsOfThisGuy(enemy: Enemy, uniqueDudeCount: Int = 4, dudeMultiplier: Int = 4) = sequence {
            val states = 4f.pow(uniqueDudeCount).toInt()
            for (code in 0 until states) { // Last state is skipped because it has an extra digit. 10000 or whatever
                val codeString = code.toString(4).padStart(uniqueDudeCount, '0')
                var template = ""
                for (n in codeString) {
                    template += (
                            when (n) {
                                '0' -> {
                                    "${enemy.name} ${enemy.maxHp} 0"
                                }
                                '1' -> {
                                    "${enemy.name} ${enemy.maxHp} ${enemy.maxHp-1}"
                                }
                                '2' -> {
                                    "${enemy.name} ${enemy.maxHp} ${enemy.maxHp}"
                                }
                                else -> {
                                    ""
                                }
                            }
                    ) + "\n"
                }
                yield(createMany(template.repeat(dudeMultiplier), 7))
            }
        }

        // TODO make the names in this more clear
        @RequiresApi(Build.VERSION_CODES.O)
        fun interestingPipisTeams(uniqueDudeCount: Int = 2, dudeMultiplier: Int = 5) = sequence {
            val teamWideVariablesToCheckCount = 4

            val teamStateCount = 2f.pow(teamWideVariablesToCheckCount).toInt() // Each digit contains 2 possibilities, and there's 4 variables

            for (interestingGuy in oneOfEachInterestingGuy()) {
                for (teamStateInt in 0 until teamStateCount) {
                    for (teamSequence in teamsOfThisGuy(interestingGuy, uniqueDudeCount, dudeMultiplier)) {
                        // Team states
                        val team = teamSequence.toList()
                        val teamStateString = teamStateInt.toString(2).padStart(teamWideVariablesToCheckCount, '0')
                        for ((n, member) in team.withIndex()) {
                            member.name += n.toString()
                            member.attackersGainDisadvantage = teamStateString[0] == '1'
                            member.poisoned = teamStateString[1] == '1'
                            member.inRetaliateRange = teamStateString[2] == '1'
                            member.inMeleeRange = teamStateString[3] == '1'
                        }
                        println(teamStateString)
                        yield(team)
                    }
                }
            }
        }

        fun interestingTeamTargeting(team: List<Enemy>, antiRedundancy: Int = 4)
        {
            val memberVariablesToCheckCount = 2
            // Member states
            val singleMemberStateCount = 2f.pow(memberVariablesToCheckCount).toInt() // Each digit contains 2 possibilities, and there's 4 variables, for 8
            val allMembersStateCount = (singleMemberStateCount).toDouble().pow(team.count() / antiRedundancy).toInt() // Each digit contains 8 possibilities, and there's (team) digits

            for (allMemberStatesInt in 0 until allMembersStateCount) {
                val allMemberStatesBase = memberVariablesToCheckCount
                val allMemberStatesString = allMemberStatesInt.toString(allMemberStatesBase)
                    .padStart(team.count() * memberVariablesToCheckCount, '0') // base 8 number

                if ((allMembersStateCount - 1).toString(memberVariablesToCheckCount).length != team.count() * memberVariablesToCheckCount) {
                    throw Exception("Fuck")
                }

                for (memberStateChar in allMemberStatesString) {  // Single base 8 character
                    val memberStateBinaryString =
                        memberStateChar.toString().toInt(allMemberStatesBase)
                            .toString(2) // The values for every nth member, n being memberStateIndex

                    for ((teamMemberIndex, teamMember) in team.withIndex()) {
                        if (teamMemberIndex % antiRedundancy != 0) {
                            continue
                        }
                        teamMember.targeted = memberStateBinaryString[0] == '1'
                        teamMember.extraTarget = memberStateBinaryString[1] == '1'
                    }
                }
            }
        }

        // TODO make the names in this more clear
//        @RequiresApi(Build.VERSION_CODES.O)
//        fun interestingPipisTeams(dudeExponent: Int = 2, dudeMultiplier: Int = 5) = sequence {
//            val encoder: java.util.Base64.Encoder = java.util.Base64.getEncoder()
//            val variablesToCheckCount = 6
//            for (interestingGuy in oneOfEachInterestingGuy()) {
//                for (teamSequence in teamsOfThisGuy(interestingGuy, dudeExponent, dudeMultiplier)) {
//                    val team = teamSequence.toList()
//                    // States are boolean values for targeted, extraTarget,
//                    // attackersGainDisadvantage, poisoned, inRetaliateRange, inMeleeRange
//                    val singleMemberStateCombinationCount = 2f.pow(variablesToCheckCount).toInt()
//                    val teamStateCombinationCount = singleMemberStateCombinationCount.toDouble().pow(team.count() / dudeMultiplier).toInt()
//                    for (state in 0 until teamStateCombinationCount) {
//                        val teamCodeString = encoder.encodeToString(state.toString().encodeToByteArray()).padStart(dudeExponent, '0')
//                        for ((memberStateIndex, memberState) in teamCodeString.withIndex()) {  // This state is given to every dudeMultiplierth dude
//                            val memberCodeString = memberState.toInt().toString(2)
//                            for ((teamMemberIndex, teamMember) in team.withIndex()) {
//                                if ((memberStateIndex + teamMemberIndex) % dudeMultiplier != 0) {
//                                    continue
//                                }
//                                teamMember.targeted = memberCodeString[0] == '1'
//                                teamMember.extraTarget = memberCodeString[1] == '1'
//                                teamMember.attackersGainDisadvantage = memberCodeString[2] == '1'
//                                teamMember.poisoned = memberCodeString[3] == '1'
//                                teamMember.inRetaliateRange = memberCodeString[4] == '1'
//                                teamMember.inMeleeRange = memberCodeString[5] == '1'
//                            }
//                        }
//                        yield(team)
//                    }
//                }
//            }
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAttacked(card: Card, player: Player) {
        if (!getTargetable(ignoreTargeted = true)) {
            throw Exception("Shouldn't be attacking an untargetable guy.")
        }
        taken += Integer.max(0, card.value - effectiveShield(player)) +
                if (poisoned and !card.nullOrCurse) 1 else 0
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun effectiveShield(player: Player): Int {
        return Integer.max(0, shield - player.pierce)
    }

    fun getHp(): Int {
        return maxHp - taken
    }

    fun getTargetable(ignoreTargeted: Boolean = false): Boolean {
        return !dead && (targeted || ignoreTargeted)
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

    fun deepCopy(): Enemy {
        return createOne(creationString, -1)
    }

    override operator fun equals(other: Any?): Boolean {
        return toString() == other.toString() && other != null && Enemy::class.java == other::class.java
    }

    override fun hashCode(): Int {
        var result = creationString.hashCode()
        result = 31 * result + taken
        result = 31 * result + maxHp
        result = 31 * result + name.hashCode()
        result = 31 * result + shield
        result = 31 * result + retaliate
        result = 31 * result + attackersGainDisadvantage.hashCode()
        result = 31 * result + inRetaliateRange.hashCode()
        result = 31 * result + inMeleeRange.hashCode()
        result = 31 * result + inBallistaRange.hashCode()
        result = 31 * result + targeted.hashCode()
        result = 31 * result + extraTarget.hashCode()
        result = 31 * result + poisoned.hashCode()
        result = 31 * result + stunned.hashCode()
        result = 31 * result + muddled.hashCode()
        return result
    }
}

fun compactedString(string: String, maxLength: Int = 4): String {
    var ret = ""
    val doubleRegex = Regex("(.)\\1")
    val vowelRegex = Regex("[aeiouy]")
    for (word in string.split(" ")) {
        var newWord = word
        var lastLength = -1
        while(newWord.length > maxLength && newWord.length != lastLength) {
            lastLength = newWord.length
            newWord = doubleRegex.replaceFirst(newWord, "\$1")
            if (lastLength == newWord.length) {
                newWord = vowelRegex.replaceFirst(newWord.reversed(), "").reversed()
            }
        }
        newWord = newWord.substring(0, Integer.min(maxLength, newWord.length))
        ret += newWord
    }
    return ret
}