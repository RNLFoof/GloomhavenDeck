package com.example.gloomhavendeck

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@RequiresApi(Build.VERSION_CODES.N)
open class Deck {
    // Cards
    var drawPile = mutableListOf<Card>()
    var activeCards = mutableListOf<Card>()
    var discardPile = mutableListOf<Card>()

    // Logging
    var logList = mutableListOf<String>()
    var logIndent = 0 // How many spaces to insert before a log, used to indicate that one action is part of another
    var logMuted = false // So you can make it shut up
    var logCount = 0 // How many logs have been made in general, used instead of index so old stuff can be removed
    var logsToHide = 0 // Used to go back and forth while undoing without like, making entire separate copies of the logs

    // Undoing
    val undoPoints = mutableListOf<UndoPoint>()
    var undosBack = 0

    open inner class UndoPoint() {
        val drawPile : MutableList<Card> = this@Deck.drawPile.toMutableList()
        val activeCards : MutableList<Card> = this@Deck.activeCards.toMutableList()
        val discardPile : MutableList<Card> = this@Deck.discardPile.toMutableList()
        var logCount = this@Deck.logCount

        open fun use() {
            this@Deck.drawPile = drawPile.toMutableList()
            this@Deck.activeCards = activeCards.toMutableList()
            this@Deck.discardPile = discardPile.toMutableList()
            logsToHide += this@Deck.logCount - logCount
            this@Deck.logCount = logCount
        }
    }

    // Meta
    open fun log(text: String) {
        Log.d("heyyyy", text)
        if (!logMuted) {
            // Override any "future" logs
            while (logsToHide > 0 && logList.size > 0) {
                logsToHide -= 1
                logList.removeLast()
            }
            logsToHide = 0 // So that if there's more to hide than there is it still resets
            logList.add("----".repeat(logIndent) + text)
            while (logList.size > 100) {
                logList.removeFirst()
            }
            logCount += 1
        }
    }

    fun getShownLogs(): MutableList<String> {
        Log.d("undos", "Hiding $logsToHide logs. Final log is ${logList.last()}")
        return logList.subList(0, max(0, logList.size-logsToHide))
    }

    fun addUndoPoint() {
        log("State saved.")
        // Override any "future" undos
        while (undosBack > 0) {
            undosBack -= 1
            undoPoints.removeLast()
        }
        // Add a new one
        undoPoints.add(getUndoPoint())
    }

    // Done like this so the object can be replaced with an expanded one.
    open fun getUndoPoint(): UndoPoint {
        return UndoPoint()
    }

    fun Undo() {
        undosBack += 1
        Log.d("undos", "Loading state ${undoPoints.size-undosBack-1+1}/${undoPoints.size}")
        undoPoints[undoPoints.size-undosBack-1].use()
    }

    fun Redo() {
        undosBack -= 1
        Log.d("undos", "Loading state ${undoPoints.size-undosBack+1}/${undoPoints.size}")
        undoPoints[undoPoints.size-undosBack-1].use()
    }

    // Adding cards
    open fun addBaseDeck() {
        addMultipleToDrawPile(listOf(
            Card(0, multiplier = true, spinny = true),
            Card(2, multiplier = true, spinny = true),

            Card(-2),
            Card(-1),
            Card(0),
            Card(1),
            Card(1),
            Card(1),
            Card(1),
            Card(1),
            Card(2),
            Card(2),

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
        addUndoPoint()
    }

    open fun addToDrawPile(card: Card) {
        drawPile.add(card)
        drawPile.shuffle()
        log("Shuffled this card into the draw pile: $card")
    }

    open fun addMultipleToDrawPile(cards: Iterable<Card>) {
        for (card in cards) {
            drawPile.add(card)
        }
        drawPile.shuffle()
        log("Shuffled these cards into the draw pile: $cards")
    }

    fun curse(userDirectlyRequested: Boolean = false) {
        log("Adding a curse...")
        logIndent += 1
        addToDrawPile(Card(0, lose = true, multiplier = true))
        logIndent -= 1
        if (userDirectlyRequested)
            addUndoPoint()
    }

    fun bless(userDirectlyRequested: Boolean = false) {
        log("Adding a bless...")
        logIndent += 1
        addToDrawPile(Card(2, lose = true, multiplier = true))
        logIndent -= 1
        if (userDirectlyRequested)
            addUndoPoint()
    }


    // Moving cards
    open fun drawSingleCard(): Card {
        if (drawPile.size == 0){
            log("Out of cards, have to dominion it...")
            logIndent += 1
            discardPileToDrawPile()
            logIndent -= 1
        }
        if (drawPile.size == 0){
            log("!!! Absorbing the active cards just to avoid crashing. Yikes!")
            logIndent += 1
            activeCardsToDiscardPile()
            discardPileToDrawPile()
            logIndent -= 1
        }
        val drewCard = drawPile.removeFirst()
        log("Drew this card: $drewCard")
        return drewCard
    }

    fun drawRow(): MutableList<Card> {
        log("Drawing a row of cards...")
        logIndent += 1
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
        logIndent -= 1
        return drawnRow
    }

    open fun activeCardsToDiscardPile(userDirectlyRequested: Boolean = false) {
        discardPile.addAll(activeCards);
        activeCards.clear()
        log("Moved the active cards to the discard pile.")
        if (userDirectlyRequested)
            addUndoPoint()
    }

    fun discardPileToDrawPile(userDirectlyRequested: Boolean = false) {
        log("Shuffling the discard pile into the draw pile...")
        logIndent += 1
        addMultipleToDrawPile(discardPile);
        discardPile.clear()
        logIndent -= 1
        if (userDirectlyRequested)
            addUndoPoint()
    }

    open fun attack(basePower: Int = 0, userDirectlyRequested: Boolean = false) : Card {
        logIndent += 1
        val drawnRow = drawRow()
        val baseCard = Card(basePower)
        drawnRow.add(0, baseCard)
        val combinedCard = drawnRow.sum()
        if (basePower == 0 && drawnRow.any{it.multiplier && it.value == 2}) {
            log("Can't infer the result without a base value, nerd.")
        } else {
            log("Effectively drew a ${combinedCard}");
        }
        logIndent -= 1;
        if (userDirectlyRequested)
            addUndoPoint()
        return combinedCard
    }

    open fun advantage(basePower: Int = 0, userDirectlyRequested: Boolean = false) : Card {
        logIndent += 1
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
            log("Can't infer the result without a base value, nerd.");
        } else {
            log("Effectively drew a ${combinedCard}");
        }
        logIndent -= 1
        if (userDirectlyRequested)
            addUndoPoint()
        return combinedCard
    }

    open fun disadvantage(basePower: Int = 0, userDirectlyRequested: Boolean = false) : Card {
        logIndent += 1
        val drawnRow1 = drawRow()
        val drawnRow2 = drawRow()
        val baseCard = Card(basePower)

        val loser = if (drawnRow1.last() < drawnRow2.last()) drawnRow1.last() else drawnRow2.last()
        val combinedCard = baseCard + loser

        if (basePower == 0 && (drawnRow1 + drawnRow2).any{it.multiplier && it.value == 2}) {
            log("Can't infer the result without a base value, nerd.");
        } else {
            log("Effectively drew a $loser")
        }
        logIndent -= 1;
        if (userDirectlyRequested)
            addUndoPoint()
        return combinedCard
    }

    fun pipis(player : Player, enemies : Iterable<Enemy>) {
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

        fun tryToDitchPendant() {
            if (player.inventory.unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
                return
            }
            player.inventory.makeRoom(player, this,2, powerPotThresholdReached(),
                powerPotAggregatedPower() > 0)
            if (player.inventory.unusableItems.size >= 2) {
                player.useItem(Item.PENDANT_OF_DARK_PACTS, this)
            }
        }

        log("Pipis...")
        val startSummary = getSummary()
        logIndent += 1
        var allowedToContinue = true
        var loops = 0
        var gotASpinny = false
        while (allowedToContinue) {
            log("")
            enemyIndex = 0
            allowedToContinue = false
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
            var basePower = if (usingBallistaInstead) 4 else 1
            if (powerPotThresholdReached() && player.inventory.usableItems.contains(Item.MAJOR_POWER_POTION)) {
                basePower += 2
                player.useItem(Item.MAJOR_POWER_POTION, this)
            }
            // Room?
            tryToDitchPendant()
            // Another potion?
            if (powerPotThresholdReached() && player.inventory.usableItems.contains(Item.MAJOR_POWER_POTION)) {
                basePower += 2
                player.useItem(Item.MAJOR_POWER_POTION, this)
            }
            // Display
            log("")
            log("Loop ${++loops}, for ${basePower}+-${if (usingBallistaInstead) ", using ballista" else ""}...")
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

                logMuted = true
                val combinedCard = if (advantage > 0) advantage(basePower)
                else if (advantage < 0) disadvantage(basePower)
                else attack(basePower)
                logMuted = false

                enemy.getAttacked(combinedCard, player)
                log("Hit ${enemy.name} with $combinedCard${if (enemy.dead) ", dies!" else ""}")
                if (combinedCard.refresh) {
                    player.inventory.recover(player, this)
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
                log("Extra target!")
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
                    log("But couldn't find an extra target.")
                }
            }
            // One more time?
            val wantToGoAgain = player.hp - enemies.sumOf { if (!it.getTargetable() || !it.inRetaliateRange) 0 else it.retaliate } >= player.hpDangerThreshold
                                && (enemies.sumOf { if (!it.getTargetable()) 0 else "1".toInt() } >= 3)
            val canGoAgain = player.inventory.usableItems.contains(Item.RING_OF_BRUTALITY)
                    && (player.inventory.usableItems.contains(Item.MINOR_STAMINA_POTION) || player.inventory.usableItems.contains(Item.MAJOR_STAMINA_POTION))
            // Do I want to?
            if (wantToGoAgain && canGoAgain) {
                // Can I?
                if (player.inventory.usableItems.contains(Item.MINOR_STAMINA_POTION)) {
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        player.useItem(Item.MINOR_STAMINA_POTION, this)
                        player.useItem(Item.RING_OF_BRUTALITY, this)
                        allowedToContinue = true
                }
                else if (player.inventory.usableItems.contains(Item.MAJOR_STAMINA_POTION)) {
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        player.useItem(Item.MAJOR_STAMINA_POTION, this)
                        player.useItem(Item.RING_OF_BRUTALITY, this)
                        allowedToContinue = true
                }
                else {
                    log("How the fuck did you get here")
                }
            }
            else if (wantToGoAgain && !canGoAgain) {
                log("Want to go again, but can't.")
            }
            else if (!wantToGoAgain && canGoAgain) {
                log("Can go again, but don't wanna.")
            }
            else {
                log("GET ME OUT OF THIS THING")
            }
            activeCardsToDiscardPile()
        }
        if (gotASpinny) {
            discardPileToDrawPile()
        }

        log("End summary:")
        logIndent += 1
        log("Gained $loops xp")
        val endSummary = getSummary()
        for (startKv in startSummary) {
            val endV = endSummary[startKv.key]
            if (startKv.value != endV) {
                if (startKv.value is Int && endV is Int) {
                    val dif = endV - (startKv.value as Int)
                    if (dif >= 0) {
                        log("${startKv.key}: ${startKv.value} -> ${endV} (+${dif})")
                    } else {
                        log("${startKv.key}: ${startKv.value} -> ${endV} (${dif})")
                    }
                }
                else {
                    log("${startKv.key}: ${startKv.value} -> ${endV}")
                }
            }
        }
        logIndent -= 1
        logIndent -= 1
        addUndoPoint()
    }
}
