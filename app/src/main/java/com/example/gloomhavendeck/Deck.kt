package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
class Deck(): Controllable() {
    init {
        Controller.deck = this
    }

    // Cards
    var drawPile = mutableListOf<Card>()
    var activeCards = mutableListOf<Card>()
    var discardPile = mutableListOf<Card>()
    var remainingCurses = 10

    // Adding cards
    fun addBaseDeckThreeSpears() {
        addMultipleToDrawPile(listOf(
            Card(0, nullOrCurse = true, spinny = true),
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
        Controller.undoManager?.addUndoPoint()
    }

    fun addBaseDeckEye() {
        addMultipleToDrawPile(listOf(
            Card(0, nullOrCurse = true, spinny = true),
            Card(2, multiplier = true, spinny = true),

            Card(0),
            Card(0),

            Card(1),
            Card(1),
            Card(1),
            Card(1, healAlly = 2),

            Card(2),
            Card(2, element = Element.DARK),
            Card(2, regenerate = true),
            Card(2, curses = true),

            Card(3, shieldSelf = 1),
            Card(3, shieldSelf = 1),
            Card(3, muddle = true),
        ))
        Controller.undoManager?.addUndoPoint()
    }

    fun addBaseDeckStandard() {
        addMultipleToDrawPile(listOf(
            Card(0, nullOrCurse = true, spinny = true),
            Card(2, multiplier = true, spinny = true),

            Card(-2),

            Card(-1),
            Card(-1),
            Card(-1),
            Card(-1),
            Card(-1),

            Card(0),
            Card(0),
            Card(0),
            Card(0),
            Card(0),
            Card(0),

            Card(1),
            Card(1),
            Card(1),
            Card(1),
            Card(1),

            Card(2),
        ))
        Controller.undoManager?.addUndoPoint()
    }

    fun addBaseDeckThreeKnives() {
        addMultipleToDrawPile(listOf(
            Card(0, nullOrCurse = true, spinny = true),
            Card(2, multiplier = true, spinny = true),

            Card(0),

            Card(1),
            Card(1),
            Card(1),
            Card(1),
            Card(1),
            Card(1),

            Card(1, flippy = true),
            Card(1, flippy = true),
            Card(1, flippy = true),
            Card(1, flippy = true),

            Card(2),
            Card(2),
            Card(2),

            Card(flippy = true, pierce = 3),
            Card(flippy = true, pierce = 3),

            Card(flippy = true, invisible = true),
        ))
        Controller.undoManager?.addUndoPoint()
    }

    fun shuffle() {
        drawPile.shuffle()
        Controller.activityConnector?.effectQueue?.addLast(Effect(sound=SoundBundle.SHUFFLE))
    }

    fun addToDrawPile(card: Card) {
        drawPile.add(card)
        shuffle()
        Controller.logger?.log("Shuffled this card into the draw pile: $card")
    }

    fun addMultipleToDrawPile(cards: Iterable<Card>) {
        for (card in cards) {
            drawPile.add(card)
        }
        shuffle()
        Controller.logger?.log("Shuffled these cards into the draw pile: $cards")
    }

    fun curse(userDirectlyRequested: Boolean = false) {
        Controller.logger?.log("Adding a curse...")
        Controller.logger?.let {it.logIndent += 1}
        if (remainingCurses > 0) {
            addToDrawPile(Card(0, nullOrCurse = true, lose = true))
            remainingCurses -= 1
        } else {
            Controller.logger?.log("JK none left")
        }
        Controller.logger?.let {it.logIndent -= 1}
        if (userDirectlyRequested)
            Controller.undoManager?.addUndoPoint()
    }

    fun bless(userDirectlyRequested: Boolean = false) {
        Controller.logger?.log("Adding a bless...")
        Controller.logger?.let {it.logIndent += 1}
        addToDrawPile(Card(2, multiplier = true, lose = true))
        Controller.logger?.let {it.logIndent -= 1}
        if (userDirectlyRequested)
            Controller.undoManager?.addUndoPoint()
    }


    // Moving cards
    fun drawSingleCard(forcedCard: Card? = null, doingDisadvantage: Boolean = false, displayBenefitsAsRemoved: Boolean = false): Card {
        if (drawPile.size == 0){
            Controller.logger?.log("Out of cards, have to dominion it...")
            Controller.logger?.let {it.logIndent += 1}
            discardPileToDrawPile()
            Controller.logger?.let {it.logIndent -= 1}
        }
        if (drawPile.size == 0){
            Controller.logger?.log("!!! Absorbing the active cards just to avoid crashing. Yikes!")
            Controller.logger?.let {it.logIndent += 1}
            activeCardsToDiscardPile()
            discardPileToDrawPile()
            Controller.logger?.let {it.logIndent -= 1}
        }
        val drewCard = if (forcedCard !== null) {
            forcedCard
        } else {
            drawPile.removeFirst()
        }
        Controller.logger?.log("Drew this card: $drewCard")

        // Add effect
        Controller.activityConnector?.effectQueue?.addLast(drewCard.effect(Controller, doingDisadvantage = doingDisadvantage, displayBenefitsAsRemoved = displayBenefitsAsRemoved))

        return drewCard
    }

    fun drawRow(nerf: Int = 0, withoutSpecialBenefits: Boolean = false, doingDisadvantage: Boolean = false): MutableList<Card> {
        Controller.logger?.log("Drawing a row of cards...")
        Controller.logger?.let {it.logIndent += 1}
        val drawnRow = mutableListOf<Card>()

        // Actual drawing
        var continueDrawing = true
        var nerfsLeft = nerf
        while (continueDrawing) {
            val latestCard = if (nerfsLeft <= 0) {
                drawSingleCard(displayBenefitsAsRemoved = withoutSpecialBenefits, doingDisadvantage=doingDisadvantage)
            } else {
                nerfsLeft -= 1
                drawSingleCard(Card(-1, lose=true, flippy=true))
            }
            continueDrawing = latestCard.flippy

            if (!latestCard.lose) {
                activeCards.add(latestCard)
            }
            if (withoutSpecialBenefits) {
                drawnRow.add(latestCard.withoutSpecialBenefits())
            } else {
                drawnRow.add(latestCard)
            }
            // Should do this a different way but too bad
            // TODO replace with curse attribute. Actually it shouldn't be tracked like this at all
            if ("curse" in latestCard.toString()) {
                remainingCurses += 1
            }
        }
        Controller.logger?.let {it.logIndent -= 1}

        Controller.activityConnector?.effectQueue?.addLast(Effect(selectBottomRow = true))

        return drawnRow
    }

    fun activeCardsToDiscardPile(userDirectlyRequested: Boolean = false) {
        discardPile.addAll(activeCards)
        activeCards.clear()
        Controller.logger?.log("Moved the active cards to the discard pile.")
        if (userDirectlyRequested) {
            Controller.undoManager?.addUndoPoint()
        }
        if (activeCards.size != 0) {
            Controller.activityConnector?.effectQueue?.addLast(Effect(sound=SoundBundle.DISCARD))
        }
        Controller.activityConnector?.effectQueue?.addLast(Effect(sound=SoundBundle.DISCARD))
    }

    fun discardPileToDrawPile(userDirectlyRequested: Boolean = false) {
        Controller.logger?.log("Shuffling the discard pile into the draw pile...")
        Controller.logger?.let {it.logIndent += 1}
        addMultipleToDrawPile(discardPile)
        discardPile.clear()
        Controller.logger?.let {it.logIndent -= 1}
        if (userDirectlyRequested)
            Controller.undoManager?.addUndoPoint()
    }

    fun attack(basePower: Int = 0, userDirectlyRequested: Boolean = false, nerf: Int = 0, withoutSpecialBenefits: Boolean = false) : Card {
        Controller.logger?.let {it.logIndent += 1}
        Controller.activityConnector?.effectQueue?.addLast(Effect(selectTopRow = true, hideBottomRow = true, wipe=true))
        val drawnRow = drawRow(nerf = nerf, withoutSpecialBenefits=withoutSpecialBenefits)
        val baseCard = Card(basePower)
        drawnRow.add(0, baseCard)

        val combinedCard = drawnRow.sum()
        if (basePower == 0 && drawnRow.any{it.multiplier && it.value == 2}) {
            Controller.logger?.log("Can't infer the result without a base value, nerd.")
        } else {
            Controller.logger?.log("Effectively drew a $combinedCard")
        }
        Controller.logger?.let {it.logIndent -= 1}
        if (userDirectlyRequested)
            Controller.undoManager?.addUndoPoint()
        return combinedCard
    }

    fun advantage(basePower: Int = 0, userDirectlyRequested: Boolean = false, nerf: Int = 0, withoutSpecialBenefits: Boolean = false) : Card {
        Controller.activityConnector?.effectQueue?.addLast(Effect(selectTopRow = true, showBottomRow = true, wipe=true))
        Controller.logger?.let {it.logIndent += 1}
        val drawnRow1 = drawRow(nerf = nerf, withoutSpecialBenefits=withoutSpecialBenefits)
        val drawnRow2 = drawRow(nerf = nerf, withoutSpecialBenefits=withoutSpecialBenefits)
        val baseCard = Card(basePower)
        drawnRow1.add(0, baseCard) // Doesn't matter which row it goes into

        // Prioritize refreshes
        val winner : Card = if (drawnRow1.last().refresh) {
            drawnRow1.last()
        } else if (drawnRow2.last().refresh) {
            drawnRow2.last()
        }
        // Otherwise..
        else {
            if (drawnRow1.last() > drawnRow2.last()) drawnRow1.last() else drawnRow2.last()
        }
        val combinedCard = (
                drawnRow1.slice(0 until drawnRow1.size-1)
                        + drawnRow2.slice(0 until drawnRow2.size-1)
                        + listOf(winner)
                ).sum()

        if (basePower == 0 && (drawnRow1 + drawnRow2).any{it.multiplier && it.value == 2}) {
            Controller.logger?.log("Can't infer the result without a base value, nerd.")
        } else {
            Controller.logger?.log("Effectively drew a $combinedCard")
        }
        Controller.logger?.let {it.logIndent -= 1}
        if (userDirectlyRequested)
            Controller.undoManager?.addUndoPoint()
        return combinedCard
    }

    fun disadvantage(basePower: Int = 0, userDirectlyRequested: Boolean = false, nerf: Int = 0, withoutSpecialBenefits: Boolean = false) : Card {
        Controller.activityConnector?.effectQueue?.addLast(Effect(selectTopRow = true, showBottomRow = true, wipe=true))
        Controller.logger?.let {it.logIndent += 1}
        val drawnRow1 = drawRow(nerf = nerf, withoutSpecialBenefits=withoutSpecialBenefits, doingDisadvantage=true)
        val drawnRow2 = drawRow(nerf = nerf, withoutSpecialBenefits=withoutSpecialBenefits, doingDisadvantage=true)
        val baseCard = Card(basePower)

        val loser = if (drawnRow1.last() < drawnRow2.last()) drawnRow1.last() else drawnRow2.last()
        var combinedCard = baseCard + loser

        // Nerf (done here as well because it's otherwise ignored bc disadvantage)
        for (i in 0..nerf) {
            combinedCard += Card(-1)
        }

        if (basePower == 0 && (drawnRow1 + drawnRow2).any{it.multiplier && it.value == 2}) {
            Controller.logger?.log("Can't infer the result without a base value, nerd.")
        } else {
            Controller.logger?.log("Effectively drew a $loser")
        }
        Controller.logger?.let {it.logIndent -= 1}
        if (userDirectlyRequested)
            Controller.undoManager?.addUndoPoint()
        return combinedCard
    }

    fun pipis(player : Player, enemies : List<Enemy>) {
        val nerflessCounterparts = Controller.pipis!!.generateNerflessCounterparts(enemies)
        if (Controller.inventory == null) {
            throw Exception("Can't pipis without an inventory")
        }
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
                if (Controller.inventory != null) {
                    vars["Inventory has $item"] = Controller.inventory!!.usableItems.contains(item)
                }
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

        Controller.logger?.log("Pipis...")
        val startSummary = getSummary()
        Controller.logger?.let {it.logIndent += 1}
        var allowedToContinue = true
        var loops = 0
        var bonusItems = 0
        var gotASpinny = false
        while (allowedToContinue) {
            Controller.logger?.log("")
            enemyIndex = 0
            allowedToContinue = false
            // Init power up here instead so that pendant can use it
            var basePower = 1
            fun tryToDitchPendant() {
                if (Controller.inventory!!.unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
                    return
                }
                val roomMade = Controller.inventory!!.makeRoom(2, powerPotThresholdReached(),
                    powerPotAggregatedPower() > 0)
                if (roomMade.contains(Item.MAJOR_POWER_POTION)) {
                    basePower += 2
                }
                if (Controller.inventory!!.unusableItems.size >= 2) {
                    Controller.inventory!!.useItem(Item.PENDANT_OF_DARK_PACTS,  true)
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
            if (powerPotThresholdReached() && Controller.inventory!!.usableItems.contains(Item.MAJOR_POWER_POTION)) {
                basePower += 2
                Controller.inventory!!.useItem(Item.MAJOR_POWER_POTION, true)
            }
            // Eye?
            if (Controller.inventory!!.usableItems.contains(Item.LUCKY_EYE) && !player.statuses.contains(Status.STRENGTHEN)) {
                Controller.inventory!!.useItem(Item.LUCKY_EYE, true)
            }
            // Room?
            tryToDitchPendant()
            // Another potion?
            if (powerPotThresholdReached() && Controller.inventory!!.usableItems.contains(Item.MAJOR_POWER_POTION)) {
                basePower += 2
                Controller.inventory!!.useItem(Item.MAJOR_POWER_POTION, true)
            }
            // Display
            Controller.logger?.log("")
            Controller.logger?.log("Loop ${++loops}, for ${basePower}+-${if (usingBallistaInstead) ", using ballista" else ""}...")
            // Attacks
            var gotExtraTarget = false
            fun attackEnemy(enemy: Enemy) {
                val counterpart = nerflessCounterparts[enemy.name]!!

                tryToDitchPendant()

                // TODO this can be a player method and a pipis method
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

                Controller.logger?.let {it.logMuted = true}
                val combinedCard = if (advantage > 0) advantage(basePower, withoutSpecialBenefits = counterpart.dead)
                else if (advantage < 0) disadvantage(basePower, withoutSpecialBenefits = counterpart.dead)
                else attack(basePower, withoutSpecialBenefits = counterpart.dead)

                Controller.logger?.let {it.logMuted = false}

                if (!counterpart.dead) {
                    counterpart.getAttacked(combinedCard, player)
                }

                val nerf = loops-1
                combinedCard.value = max(0, combinedCard.value-nerf)
                enemy.getAttacked(combinedCard, player)

                Controller.logger?.log("Hit ${enemy.name} with $combinedCard${if (enemy.dead) ", dies!" else ""}")
                if (combinedCard.refresh) {
                    Controller.inventory!!.recover()
                    if (Controller.activityConnector != null) {
                        Controller.activityConnector!!.effectSpeed = Controller.activityConnector!!.effectSpeed*7/10
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
                Controller.logger?.log("Extra target!")
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
                    Controller.logger?.log("But couldn't find an extra target.")
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
                        && loops <= 10
                canGoAgain = Controller.inventory!!.usableItems.contains(Item.RING_OF_BRUTALITY)
                        && (Controller.inventory!!.usableItems.contains(Item.MINOR_STAMINA_POTION) || Controller.inventory!!.usableItems.contains(Item.MAJOR_STAMINA_POTION))
            }
            setWantAndCan()
            // Maybe use belt
            if (wantToGoAgain && !canGoAgain ) {
                if (Controller.inventory!!.usableItems.contains(Item.UTILITY_BELT)) {
                    // Belt will recover pendant, pendant will automatically recover major and ring
                    Controller.inventory!!.useItem(Item.UTILITY_BELT, true)
                    setWantAndCan()
                }
            }
            // Maybe get a bonus ball
            if (Controller.inventory!!.unusableItems.size == 0 && Controller.inventory!!.usableItems.contains(Item.UTILITY_BELT)) {
                Controller.inventory!!.useItem(Item.UTILITY_BELT, false)
                bonusItems += 1
            }
            // Ok go
            if (wantToGoAgain && canGoAgain) {
                // Can I?
                if (Controller.inventory!!.usableItems.contains(Item.MINOR_STAMINA_POTION)) {
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        Controller.inventory!!.useItem(Item.MINOR_STAMINA_POTION, true)
                        Controller.inventory!!.useItem(Item.RING_OF_BRUTALITY, true)
                        allowedToContinue = true
                }
                else if (Controller.inventory!!.usableItems.contains(Item.MAJOR_STAMINA_POTION)) {
                        if (shouldUseBallista() && player.discardedBallista) {
                            player.discardedBallista = false
                        }
                        if (!shouldUseBallista() && player.discardedPipis) {
                            player.discardedPipis = false
                        }
                        Controller.inventory!!.useItem(Item.MAJOR_STAMINA_POTION, true)
                        Controller.inventory!!.useItem(Item.RING_OF_BRUTALITY, true)
                        allowedToContinue = true
                }
                else {
                    Controller.logger?.log("How the fuck did you get here")
                }
            }
            else if (wantToGoAgain && !canGoAgain) {
                Controller.logger?.log("Want to go again, but can't.")
            }
            else if (!wantToGoAgain && canGoAgain) {
                Controller.logger?.log("Can go again, but don't wanna.")
            }
            else {
                Controller.logger?.log("GET ME OUT OF THIS THING")
            }
            activeCardsToDiscardPile()
        }
        if (gotASpinny) {
            discardPileToDrawPile()
        }

        Controller.logger?.log("End summary:")
        Controller.logger?.let {it.logIndent += 1}
        Controller.logger?.log("Bonus items for your allies: $bonusItems")
        player.dings += loops
        val endSummary = getSummary()
        for (startKv in startSummary) {
            val endV = endSummary[startKv.key]
            if (startKv.value != endV) {
                if (startKv.value is Int && endV is Int) {
                    val dif = endV - (startKv.value as Int)
                    if (dif >= 0) {
                        Controller.logger?.log("${startKv.key}: ${startKv.value} -> $endV (+${dif})")
                    } else {
                        Controller.logger?.log("${startKv.key}: ${startKv.value} -> $endV (${dif})")
                    }
                }
                else {
                    Controller.logger?.log("${startKv.key}: ${startKv.value} -> $endV")
                }
            }
        }
        Controller.logger?.let {it.logIndent -= 1}
        Controller.logger?.let {it.logIndent -= 1}
        Controller.undoManager?.addUndoPoint()
    }
}
