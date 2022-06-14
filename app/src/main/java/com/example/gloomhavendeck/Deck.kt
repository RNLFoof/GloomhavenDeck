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

        class RecoveryCandidate(var item: Item, var useImmediately: Boolean)
        fun getRecoveryCandidates() = sequence {
            while (true) {
                if (player.unusableItems.contains(Item.MAJOR_POWER_POTION)) {
                    yield(RecoveryCandidate(Item.MAJOR_POWER_POTION, false))
                    continue
                }
                if (player.unusableItems.contains(Item.RING_OF_BRUTALITY)) {
                    yield(RecoveryCandidate(Item.RING_OF_BRUTALITY, false))
                    continue
                }
                if (player.unusableItems.contains(Item.MAJOR_CURE_POTION)
                    && (
                            player.statuses.contains(Status.MUDDLE)
                            || player.statuses.contains(Status.POISON)
                        )
                    ) {
                    yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, true))
                    continue
                }
                if (player.unusableItems.contains(Item.SUPER_HEALING_POTION)
                    && player.hp <= player.hpDangerThreshold) {
                    yield(RecoveryCandidate(Item.SUPER_HEALING_POTION, true))
                    continue
                }
                if (player.unusableItems.contains(Item.MAJOR_POWER_POTION)) {
                    yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, false))
                    continue
                }
                if (player.unusableItems.contains(Item.MAJOR_CURE_POTION)) {
                    yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, false))
                    continue
                }
                if (player.unusableItems.contains(Item.SUPER_HEALING_POTION)) {
                    yield(RecoveryCandidate(Item.SUPER_HEALING_POTION, player.hp <= 19))
                    continue
                }
                if (player.unusableItems.contains(Item.RING_OF_SKULLS)) {
                    yield(RecoveryCandidate(Item.RING_OF_SKULLS, false))
                    continue
                }
                if (player.unusableItems.contains(Item.MINOR_STAMINA_POTION)) {
                    yield(RecoveryCandidate(Item.MINOR_STAMINA_POTION, false))
                    continue
                }
                if (player.unusableItems.contains(Item.TOWER_SHIELD)) {
                    yield(RecoveryCandidate(Item.TOWER_SHIELD, false))
                    continue
                }
                if (player.unusableItems.contains(Item.SPIKED_SHIELD)) {
                    yield(RecoveryCandidate(Item.SPIKED_SHIELD, false))
                    continue
                }
                break
            }
        }

        fun recover(viaPendant: Boolean =false) {
            // Pendant logic is kept separate because otherwise it could try to revive itself
            // and other such nonsense
            // Separate from makeRoom because the items you'd use if forced to in order to make room
            // generally aren't the same as the items you'd want to replenish ASAP if already used

            // viaPendant is for when it wasn't recovered with a card, but rather is going "I need
            // to stop having a pendant", in which case exactly two items must be used
            val itemsRecovered = mutableListOf<RecoveryCandidate>()
            for (recoveryCandidate in getRecoveryCandidates()) {
                itemsRecovered.add(recoveryCandidate)
                player.usableItems.add(recoveryCandidate.item)
                player.unusableItems.remove(recoveryCandidate.item)
                if (itemsRecovered.size >= 2) {
                    if (!player.usableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
                        player.usableItems.add(Item.PENDANT_OF_DARK_PACTS)
                        player.unusableItems.remove(Item.PENDANT_OF_DARK_PACTS)
                    }
                    break
                }
            }
            if (!viaPendant || itemsRecovered.size == 2) {
                player.useItem(Item.PENDANT_OF_DARK_PACTS)
                for (recoveryCandidate in itemsRecovered) {
                    if (recoveryCandidate.useImmediately) {
                        player.useItem(recoveryCandidate.item)
                        log("Recovered and immediately used ${recoveryCandidate.item}")
                    } else {
                        log("Recovered ${recoveryCandidate.item}")
                    }
                    curse() // For the pendant
                }
            }
            if (itemsRecovered.size == 0) {
                if (player.unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
                    player.usableItems.add(Item.PENDANT_OF_DARK_PACTS)
                    player.unusableItems.remove(Item.PENDANT_OF_DARK_PACTS)
                }
                else {
                    log("Nothing to recover :c")
                }
            }
        }

        fun getRoomMakingItems() = sequence{
            while (true) {
                if (player.usableItems.contains(Item.MAJOR_CURE_POTION)
                    && (
                            player.statuses.contains(Status.MUDDLE)
                            || player.statuses.contains(Status.POISON)
                        )
                ) {
                    yield(Item.MAJOR_CURE_POTION)
                    continue
                }
                if (player.usableItems.contains(Item.SUPER_HEALING_POTION)
                    && player.hp <= player.maxHp-7) {
                    yield(Item.SUPER_HEALING_POTION)
                    continue
                }
                if (player.usableItems.contains(Item.MAJOR_CURE_POTION)
                    && player.statuses.any{it.negative}
                ) {
                    yield(Item.MAJOR_CURE_POTION)
                    continue
                }
                // Johnson should go here
                if (player.usableItems.contains(Item.MAJOR_POWER_POTION)
                    && powerPotThresholdReached()
                ) {
                    yield(Item.MAJOR_POWER_POTION)
                    continue
                }
                if (player.usableItems.contains(Item.SUPER_HEALING_POTION)
                    && player.hp < player.maxHp
                ) {
                    yield(Item.SUPER_HEALING_POTION)
                    continue
                }
                if (player.usableItems.contains(Item.MINOR_STAMINA_POTION)
                    && player.discardedCards >= 2
                ) {
                    yield(Item.MINOR_STAMINA_POTION)
                    continue
                }
                if (player.usableItems.contains(Item.MAJOR_POWER_POTION)
                    && powerPotAggregatedPower() > 0
                ) {
                    yield(Item.MAJOR_POWER_POTION)
                    continue
                }
                break
            }
        }

        fun makeRoomAndUsePendant() {
            // Attempting to, in pendant is recovered at the start of an attack, make there be two
            // used items to use the pendant on
            // Separate from recover because the items you'd use if forced to in order to make room
            // generally aren't the same as the items you'd want to replenish ASAP if already used
            if (!player.usableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
                return
            }
            val itemsConsumed = mutableListOf<Item>()
            for (roomMakingItem in getRoomMakingItems()) {
                // Put at the start so that if it's already fine it just bails
                if (player.unusableItems.filter { it != Item.PENDANT_OF_DARK_PACTS }.size >= 2) {
                    break
                }
                player.useItem(roomMakingItem)
                log("Used $roomMakingItem in order to free up some room.")
                itemsConsumed.add(roomMakingItem)
            }
            if (player.unusableItems.filter { it != Item.PENDANT_OF_DARK_PACTS }.size >= 2) {
                recover(viaPendant=true)
            }
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
                vars["Inventory has $item"] = player.usableItems.contains(item)
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

        log("Pipis...")
        val startSummary = getSummary()
        logIndent += 1
        var arbitraryCardsRecovered = 0
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
            if (powerPotThresholdReached() && player.usableItems.contains(Item.MAJOR_POWER_POTION)) {
                basePower += 2
                player.useItem(Item.MAJOR_POWER_POTION)
            }
            // Room?
            makeRoomAndUsePendant()
            // Display
            log("")
            log("Loop ${++loops}, for ${basePower}+-${if (usingBallistaInstead) ", using ballista" else ""}...")
            // Attacks
            var gotExtraTarget = false
            fun attackEnemy(enemy: Enemy) {
                // log("Targeting ($enemy) with $basePower${if (usingBallistaInstead) " (using ballista)" else ""}...")
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

                logMuted = true
                val combinedCard = if (advantage > 0) advantage(basePower)
                else if (advantage < 0) disadvantage(basePower)
                else attack(basePower)
                logMuted = false

                enemy.getAttacked(combinedCard, player)
                log("Hit ${enemy.name} with $combinedCard${if (enemy.dead) ", dies!" else ""}")
                if (combinedCard.refresh) {
                    recover()
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
            val canGoAgain = player.usableItems.contains(Item.RING_OF_BRUTALITY)
                    && (player.usableItems.contains(Item.MINOR_STAMINA_POTION) || player.usableItems.contains(Item.MAJOR_STAMINA_POTION))
            // Do I want to?
            if (wantToGoAgain && canGoAgain) {
                // Can I?
                if (player.usableItems.contains(Item.MINOR_STAMINA_POTION)) {
                        arbitraryCardsRecovered += 1
                        player.discardedCards -= 2
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        player.useItem(Item.MINOR_STAMINA_POTION)
                        player.useItem(Item.RING_OF_BRUTALITY)
                        allowedToContinue = true
                }
                else if (player.usableItems.contains(Item.MAJOR_STAMINA_POTION)) {
                        arbitraryCardsRecovered += 2
                        player.discardedCards -= 3
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        player.useItem(Item.MAJOR_STAMINA_POTION)
                        player.useItem(Item.RING_OF_BRUTALITY)
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
        log("Recovered $arbitraryCardsRecovered arbitrary card(s)")
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
