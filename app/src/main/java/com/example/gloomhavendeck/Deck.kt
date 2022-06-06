package com.example.gloomhavendeck

import android.util.Log

open class Deck {
    // Cards
    var drawPile = mutableListOf<Card>()
    var activeCards = mutableListOf<Card>()
    var discardPile = mutableListOf<Card>()

    // Logging
    var logList = mutableListOf<String>()
    var logIndent = 0 // How many spaces to insert before a log, used to indicate that one action is part of another
    var logCount = 0 // How many logs have been made in general, used instead of index so old stuff can be removed
    var logsToHide = 0 // Used to go back and forth while undoing without like, making entire separate copies of the logs

    // Undoing
    val undoPoints = mutableListOf<UndoPoint>()
    var undosBack = 0

    inner class UndoPoint() {
        val drawPile : MutableList<Card> = this@Deck.drawPile.toMutableList()
        val activeCards : MutableList<Card> = this@Deck.activeCards.toMutableList()
        val discardPile : MutableList<Card> = this@Deck.discardPile.toMutableList()
        var logCount = this@Deck.logCount

        fun use() {
            this@Deck.drawPile = drawPile.toMutableList()
            this@Deck.activeCards = activeCards.toMutableList()
            this@Deck.discardPile = discardPile.toMutableList()
            logsToHide += this@Deck.logCount - logCount
            this@Deck.logCount = logCount
        }
    }

    // Meta
    open fun log(text: String) {
        logList.add("----".repeat(logIndent) + text)
        while (logList.size > 100) {
            logList.removeFirst()
        }
        logCount += 1
    }

    fun getShownLogs(): MutableList<String> {
        Log.d("undos", "Hiding $logsToHide logs. Final log is ${logList.last()}")
        return logList.subList(0, logList.size-logsToHide)
    }

    fun addUndoPoint() {
        log("State saved.")
        // Override any "future" undos
        while (undosBack > 0) {
            undosBack -= 1
            undoPoints.removeLast()
        }
        // Override any "future" logs
        while (logsToHide > 0) {
            logsToHide -= 1
            logList.removeLast()
        }
        // Add a new one
        undoPoints.add(UndoPoint())
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
        log("Pipis...")
        logIndent += 1
        var arbitraryCardsRecovered = 0
        var allowedToContinue = true
        while (allowedToContinue) {
            allowedToContinue = false
            // Attacks
            for (enemy in enemies) {
                log("Targeting ($enemy)...")
                if (!enemy.dead) {
                    var advantage = 0
                    if (player.statuses.contains(Status.STRENGTHEN)) {
                        advantage += 1
                    }
                    if (player.statuses.contains(Status.MUDDLE)) {
                        advantage -= 1
                    }
                    if (enemy.inMeleeRange) { // Make sure to set this when ballista is added
                        advantage -= 1
                    }

                    val combinedCard =   if (advantage > 0) advantage(1)
                                        else if (advantage < 0) disadvantage(1)
                                        else attack(1)
                    enemy.getAttacked(combinedCard, player)
                    log("Used a $combinedCard, resulting in ($enemy)")
                }
            }
            // One more time?
            if (player.usableItems.contains(Item.MINOR_STAMINA_POTION)
                && player.usableItems.contains(Item.RING_OF_BRUTALITY)
                ) {
                arbitraryCardsRecovered += 1
                player.useItem(Item.MINOR_STAMINA_POTION)
                player.useItem(Item.RING_OF_BRUTALITY)
                allowedToContinue = true
            }
            else if (player.usableItems.contains(Item.MAJOR_STAMINA_POTION)
                && player.usableItems.contains(Item.RING_OF_BRUTALITY)
                ) {
                arbitraryCardsRecovered += 2
                player.useItem(Item.MAJOR_STAMINA_POTION)
                player.useItem(Item.RING_OF_BRUTALITY)
                allowedToContinue = true
            }
        }
        log("Recovered $arbitraryCardsRecovered arbitrary card(s)")
        logIndent -= 1
        addUndoPoint()
    }
}
