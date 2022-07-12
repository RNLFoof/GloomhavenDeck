package com.example.gloomhavendeck

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
open class Deck(@Transient var controller: Controller? = null) {
    // Cards
    var drawPile = mutableListOf<Card>()
    var activeCards = mutableListOf<Card>()
    var discardPile = mutableListOf<Card>()

    // Adding cards
    open fun addBaseDeck() {
        addMultipleToDrawPile(listOf(
            Card(0, multiplier = true, spinny = true),
            Card(2, multiplier = true, spinny = true),

            Card(-2),
            Card(-1),
            Card(1),
            Card(1),
            Card(1),
            Card(1),
            Card(1),
            Card(2),
            Card(2),
            Card(2),

            Card(1, flippy = true),
            Card(1, flippy = true),
            Card(1, flippy = true),
            Card(1, flippy = true),

            Card(flippy = true, stun = true),
            Card(flippy = true, muddle = true),
            Card(flippy = true, muddle = true),
            Card(flippy = true, muddle = true),

            Card(flippy = true, pierce = 3),
            Card(flippy = true, pierce = 3),

            Card(flippy = true, extraTarget = true),

            Card(refresh = true),
            Card(refresh = true),
            Card(refresh = true),
        ))
        controller!!.addUndoPoint()
    }

    open fun addToDrawPile(card: Card) {
        drawPile.add(card)
        drawPile.shuffle()
        controller!!.log("Shuffled this card into the draw pile: $card")
    }

    open fun addMultipleToDrawPile(cards: Iterable<Card>) {
        for (card in cards) {
            drawPile.add(card)
        }
        drawPile.shuffle()
        controller!!.log("Shuffled these cards into the draw pile: $cards")
    }

    fun curse(userDirectlyRequested: Boolean = false) {
        controller!!.log("Adding a curse...")
        controller!!.logIndent += 1
        addToDrawPile(Card(0, lose = true, multiplier = true))
        controller!!.logIndent -= 1
        if (userDirectlyRequested)
            controller!!.addUndoPoint()
    }

    fun bless(userDirectlyRequested: Boolean = false) {
        controller!!.log("Adding a bless...")
        controller!!.logIndent += 1
        addToDrawPile(Card(2, lose = true, multiplier = true))
        controller!!.logIndent -= 1
        if (userDirectlyRequested)
            controller!!.addUndoPoint()
    }


    // Moving cards
    open fun drawSingleCard(): Card {
        if (drawPile.size == 0){
            controller!!.log("Out of cards, have to dominion it...")
            controller!!.logIndent += 1
            discardPileToDrawPile()
            controller!!.logIndent -= 1
        }
        if (drawPile.size == 0){
            controller!!.log("!!! Absorbing the active cards just to avoid crashing. Yikes!")
            controller!!.logIndent += 1
            activeCardsToDiscardPile()
            discardPileToDrawPile()
            controller!!.logIndent -= 1
        }
        val drewCard = drawPile.removeFirst()
        controller!!.log("Drew this card: $drewCard")
        return drewCard
    }

    fun drawRow(): MutableList<Card> {
        controller!!.log("Drawing a row of cards...")
        controller!!.logIndent += 1
        val drawnRow = mutableListOf<Card>()
        var continueDrawing = true
        while (continueDrawing) {
            val latestCard = drawSingleCard()
            continueDrawing = latestCard.flippy
            drawnRow.add(latestCard)
            if (!latestCard.lose) {
                activeCards.add(latestCard)
            }
        }
        // log("Overall, drew this row of cards: $drawnRow")
        controller!!.logIndent -= 1
        return drawnRow
    }

    open fun activeCardsToDiscardPile(userDirectlyRequested: Boolean = false) {
        discardPile.addAll(activeCards);
        activeCards.clear()
        controller!!.log("Moved the active cards to the discard pile.")
        if (userDirectlyRequested)
            controller!!.addUndoPoint()
    }

    fun discardPileToDrawPile(userDirectlyRequested: Boolean = false) {
        controller!!.log("Shuffling the discard pile into the draw pile...")
        controller!!.logIndent += 1
        addMultipleToDrawPile(discardPile);
        discardPile.clear()
        controller!!.logIndent -= 1
        if (userDirectlyRequested)
            controller!!.addUndoPoint()
    }

    open fun attack(basePower: Int = 0, userDirectlyRequested: Boolean = false) : Card {
        controller!!.logIndent += 1
        val drawnRow = drawRow()
        val baseCard = Card(basePower)
        drawnRow.add(0, baseCard)
        val combinedCard = drawnRow.sum()
        if (basePower == 0 && drawnRow.any{it.multiplier && it.value == 2}) {
            controller!!.log("Can't infer the result without a base value, nerd.")
        } else {
            controller!!.log("Effectively drew a ${combinedCard}");
        }
        controller!!.logIndent -= 1;
        if (userDirectlyRequested)
            controller!!.addUndoPoint()
        return combinedCard
    }

    open fun advantage(basePower: Int = 0, userDirectlyRequested: Boolean = false) : Card {
        controller!!.logIndent += 1
        val drawnRow1 = drawRow()
        val drawnRow2 = drawRow()
        val baseCard = Card(basePower)
        drawnRow1.add(0, baseCard) // Doesn't matter which row it goes into

        val winner : Card
        // Prioritize refreshes
        if (drawnRow1.last().refresh) {
            winner = drawnRow1.last()
        }
        else if (drawnRow2.last().refresh) {
            winner = drawnRow2.last()
        }
        // Otherwise..
        else {
            winner = if (drawnRow1.last() > drawnRow2.last()) drawnRow1.last() else drawnRow2.last()
        }
        val combinedCard = (
                drawnRow1.slice(0 until drawnRow1.size-1)
                        + drawnRow2.slice(0 until drawnRow2.size-1)
                        + listOf(winner)
                ).sum()

        if (basePower == 0 && (drawnRow1 + drawnRow2).any{it.multiplier && it.value == 2}) {
            controller!!.log("Can't infer the result without a base value, nerd.");
        } else {
            controller!!.log("Effectively drew a ${combinedCard}");
        }
        controller!!.logIndent -= 1
        if (userDirectlyRequested)
            controller!!.addUndoPoint()
        return combinedCard
    }

    open fun disadvantage(basePower: Int = 0, userDirectlyRequested: Boolean = false) : Card {
        controller!!.logIndent += 1
        val drawnRow1 = drawRow()
        val drawnRow2 = drawRow()
        val baseCard = Card(basePower)

        val loser = if (drawnRow1.last() < drawnRow2.last()) drawnRow1.last() else drawnRow2.last()
        val combinedCard = baseCard + loser

        if (basePower == 0 && (drawnRow1 + drawnRow2).any{it.multiplier && it.value == 2}) {
            controller!!.log("Can't infer the result without a base value, nerd.");
        } else {
            controller!!.log("Effectively drew a $loser")
        }
        controller!!.logIndent -= 1;
        if (userDirectlyRequested)
            controller!!.addUndoPoint()
        return combinedCard
    }

    fun pipis(player : Player, enemies : Iterable<Enemy>, mainactivity: MainActivity? = null) {
        var enemyIndex = 0
        fun powerPotAggregatedPower() : Int {
            var aggregatedPower = 0
            var index = 0
            for (enemy in enemies) {
                if (enemy.getTargetable()) {
                    if (index++ >= enemyIndex) { // Enemy index is 0 before anything else happens
                        aggregatedPower += min(
                            enemy.getHp(),
                            max(0, 2 - enemy.effectiveShield(player))
                        )
                    }
                }
            }
            return aggregatedPower
        }

        fun powerPotThresholdReached() : Boolean {
            return powerPotAggregatedPower() >= player.powerPotionThreshold
        }

        fun getSummary(): LinkedHashMap<String, Any> {
            val vars = LinkedHashMap<String, Any>()
            // Player
            for (property in Player::class.memberProperties) {
                if (property.returnType in listOf(Boolean::class.createType(), Integer::class.createType())
                    ) {
                    try {
                    vars["Player ${property.name}"] = property.getter.call(player) as Any} catch (e: Exception) {}
                }
            }
            // Player Statuses
            for (status in Status.values()) {
                vars["Player has $status"] = player.statuses.contains(status)
            }
            // Player Items
            for (item in Item.values()) {
                vars["Inventory has $item"] = player.inventory.usableItems.contains(item)
            }
            // Enemy
            for (enemy in enemies) {
                for (property in Enemy::class.memberProperties) {
                    if (property.returnType in listOf(Boolean::class.createType(), Integer::class.createType())) {
                        vars["${enemy.name} ${property.name}"] = property.getter.call(enemy) as Any
                    }
                }
            }
            return vars
        }

        fun shouldUseBallista(): Boolean {
            return enemies.filter { it.getTargetable() }.all { it.inBallistaRange }
        }

        controller!!.log("Pipis...")
        val startSummary = getSummary()
        controller!!.logIndent += 1
        var allowedToContinue = true
        var loops = 0
        var gotASpinny = false
        while (allowedToContinue) {
            controller!!.log("")
            enemyIndex = 0
            allowedToContinue = false
            // Init power up here instead so that pendant can use it
            var basePower = 1
            fun tryToDitchPendant() {
                if (player.inventory.unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
                    return
                }
                val roomMade = player.inventory.makeRoom(player, this,2, powerPotThresholdReached(),
                    powerPotAggregatedPower() > 0)
                if (roomMade.contains(Item.MAJOR_POWER_POTION)) {
                    basePower += 2
                }
                if (player.inventory.unusableItems.size >= 2) {
                    player.useItem(Item.PENDANT_OF_DARK_PACTS, this, true)
                }
            }

            val usingBallistaInstead = shouldUseBallista() && !player.discardedBallista
            // EAT CARD
            if (usingBallistaInstead) {
                player.discardedCards += 1 // Done in this order to avoid unwanted extra changes
                player.discardedBallista = true
            } else {
                player.discardedCards += 1
                player.discardedPipis = true
            }
            // Power?
            if (usingBallistaInstead) {basePower += 3}
            if (powerPotThresholdReached() && player.inventory.usableItems.contains(Item.MAJOR_POWER_POTION)) {
                basePower += 2
                player.useItem(Item.MAJOR_POWER_POTION, this, true)
            }
            // Eye?
            if (player.inventory.usableItems.contains(Item.LUCKY_EYE) && !player.statuses.contains(Status.STRENGTHEN)) {
                player.useItem(Item.LUCKY_EYE, this, true)
            }
            // Room?
            tryToDitchPendant()
            // Another potion?
            if (powerPotThresholdReached() && player.inventory.usableItems.contains(Item.MAJOR_POWER_POTION)) {
                basePower += 2
                player.useItem(Item.MAJOR_POWER_POTION, this, true)
            }
            // Display
            controller!!.log("")
            controller!!.log("Loop ${++loops}, for ${basePower}+-${if (usingBallistaInstead) ", using ballista" else ""}...")
            // Attacks
            var gotExtraTarget = false
            fun attackEnemy(enemy: Enemy) {
                // Probably a reasonable limit
                if (!(this.activeCards.filter { it.refresh }.size < 3
                    && this.drawPile.size >= 5)
                ) {
                    tryToDitchPendant()
                }
                // ok anyway
                var advantage = 0
                if (player.statuses.contains(Status.STRENGTHEN)) {
                    advantage += 1
                }
                if (player.statuses.contains(Status.MUDDLE)) {
                    advantage -= 1
                }
                if (enemy.inMeleeRange and !usingBallistaInstead) {
                    advantage -= 1
                }
                if (enemy.attackersGainDisadvantage) {
                    advantage -= 1
                }

                controller!!.logMuted = true
                val combinedCard = if (advantage > 0) advantage(basePower)
                else if (advantage < 0) disadvantage(basePower)
                else attack(basePower)
                controller!!.logMuted = false

                enemy.getAttacked(combinedCard, player)
                controller!!.log("Hit ${enemy.name} with $combinedCard${if (enemy.dead) ", dies!" else ""}")
                if (combinedCard.refresh) {
                    player.inventory.recover(player, this)
                    if (mainactivity!=null) {
                        mainactivity.effectSpeed = mainactivity.effectSpeed*9/10
                    }
                }
                if (combinedCard.extraTarget) {
                    gotExtraTarget = true
                }
                if (combinedCard.extraTarget) {
                    gotASpinny = true
                }
                enemyIndex += 1
            }

            // Basic attacks
            for (enemy in enemies) {
                if (enemy.getTargetable()) {
                    attackEnemy(enemy)
                }
            }
            // Extra
            if (gotExtraTarget) {
                controller!!.log("Extra target!")
                var foundExtraTarget = false
                for (enemy in enemies) {
                    if (enemy.getTargetable(ignoreTargeted = true) && enemy.extraTarget &&
                        (!usingBallistaInstead || enemy.inMeleeRange)
                    ) {
                        attackEnemy(enemy)
                        foundExtraTarget = true
                        break
                    }
                }
                if (!foundExtraTarget) {
                    controller!!.log("But couldn't find an extra target.")
                }
            }
            // One more time?
            var wantToGoAgain = false
            var canGoAgain = false
            fun setWantAndCan() {
                wantToGoAgain = player.hp - enemies.sumOf { if (!it.getTargetable() || !it.inRetaliateRange) 0 else it.retaliate } >= player.hpDangerThreshold
                        && (
                            (enemies.sumOf { if (!it.getTargetable()) 0 else "1".toInt() } >= 3)
                            || (player.statuses.contains(Status.STRENGTHEN) &&enemies.sumOf { if (!it.getTargetable()) 0 else "1".toInt() } >= 2)
                        )
                canGoAgain = player.inventory.usableItems.contains(Item.RING_OF_BRUTALITY)
                        && (player.inventory.usableItems.contains(Item.MINOR_STAMINA_POTION) || player.inventory.usableItems.contains(Item.MAJOR_STAMINA_POTION))
            }
            setWantAndCan()
            // Maybe use belt
            if (wantToGoAgain && !canGoAgain) {
                if (player.inventory.usableItems.contains(Item.UTILITY_BELT)) {
                    // Belt will recover pendant, pendant will automatically recover major and ring
                    player.useItem(Item.UTILITY_BELT, this, true)
                    setWantAndCan()
                }
            }
            // Ok go
            if (wantToGoAgain && canGoAgain) {
                // Can I?
                if (player.inventory.usableItems.contains(Item.MINOR_STAMINA_POTION)) {
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        player.useItem(Item.MINOR_STAMINA_POTION, this, true)
                        player.useItem(Item.RING_OF_BRUTALITY, this, true)
                        allowedToContinue = true
                }
                else if (player.inventory.usableItems.contains(Item.MAJOR_STAMINA_POTION)) {
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        player.useItem(Item.MAJOR_STAMINA_POTION, this, true)
                        player.useItem(Item.RING_OF_BRUTALITY, this, true)
                        allowedToContinue = true
                }
                else {
                    controller!!.log("How the fuck did you get here")
                }
            }
            else if (wantToGoAgain && !canGoAgain) {
                controller!!.log("Want to go again, but can't.")
            }
            else if (!wantToGoAgain && canGoAgain) {
                controller!!.log("Can go again, but don't wanna.")
            }
            else {
                controller!!.log("GET ME OUT OF THIS THING")
            }
            activeCardsToDiscardPile()
        }
        if (gotASpinny) {
            discardPileToDrawPile()
        }

        controller!!.log("End summary:")
        controller!!.logIndent += 1
        player.dings += loops
        val endSummary = getSummary()
        for (startKv in startSummary) {
            val endV = endSummary[startKv.key]
            if (startKv.value != endV) {
                if (startKv.value is Int && endV is Int) {
                    val dif = endV - (startKv.value as Int)
                    if (dif >= 0) {
                        controller!!.log("${startKv.key}: ${startKv.value} -> ${endV} (+${dif})")
                    } else {
                        controller!!.log("${startKv.key}: ${startKv.value} -> ${endV} (${dif})")
                    }
                }
                else {
                    controller!!.log("${startKv.key}: ${startKv.value} -> ${endV}")
                }
            }
        }
        controller!!.logIndent -= 1
        controller!!.logIndent -= 1
        controller!!.addUndoPoint()
    }
}
