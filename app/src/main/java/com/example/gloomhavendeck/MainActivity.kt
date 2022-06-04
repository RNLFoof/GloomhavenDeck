package com.example.gloomhavendeck

import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var tvLog : TextView
    lateinit var llTopCardRow : LinearLayout
    lateinit var llBottomCardRow : LinearLayout
    lateinit var btnSpinny : Button
    lateinit var btnDiscard : Button
    lateinit var btnUndo : Button
    lateinit var btnRedo : Button

    lateinit var deck : Deck
    var effectQueue = LinkedList<Effect>()
    lateinit var selectedCardRow : LinearLayout
    var currentlyDoingDisadvantage = false

    inner class Effect(var sound: Int? = null, var card: Int? = null, var wipe: Boolean = false,
                       var selectTopRow: Boolean = false, var selectBottomRow: Boolean = false,
                       var showBottomRow: Boolean = false, var hideBottomRow: Boolean = false) {
        fun run() {
            if (sound != null) {
                val player: MediaPlayer = MediaPlayer.create(this@MainActivity, sound!!)
                player.start()
            }
            if (card != null) {
                val imageView = ImageView(this@MainActivity)
                imageView.setImageResource(card!!)
                imageView.rotation = (Random().nextFloat()*1-0.5).toFloat() // This masks bad scanning lol
                imageView.adjustViewBounds = true
                // Show the bottom row if there's already three cards
                // If it's already visible then whatever
                if (llTopCardRow.size == 3) {
                    showBottomRow = true
                }
                runOnUiThread {
                    selectedCardRow.addView(imageView)
                }
            }
            if (wipe) {
                runOnUiThread {
                    llTopCardRow.removeAllViews()
                    llBottomCardRow.removeAllViews()
                }
            }
            if (selectTopRow) {
                selectedCardRow = llTopCardRow
            }
            if (selectBottomRow) {
                selectedCardRow = llBottomCardRow
            }

            if (showBottomRow) {
                runOnUiThread {
                llTopCardRow.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0.toFloat()
                )}
            }
            if (hideBottomRow) {
                runOnUiThread {
                llTopCardRow.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0.0.toFloat()
                )}
            }
        }
    }

    inner class MainActivityDeck() : Deck() {

        val effectLoop = Thread {
            while (true) {
                if (effectQueue.size > 0) {
                    val effect = effectQueue.remove()
                    effect.run()
                    if (effect.sound != null) {
                        Thread.sleep(1_000/3)
                    }
                }
                else {
                    Thread.sleep(1_000/3)
                }
            }
        }
        init {effectLoop.start()}

        override fun drawSingleCard(): Card {
            val card = super.drawSingleCard()
            val effectToAdd : Effect

            // Named
            effectToAdd = if ("null" in card.toString())
                Effect(sound=R.raw.buzzer, card=R.drawable.card_null)
            else if ("curse" in card.toString())
                Effect(sound=R.raw.ttyd_ghost, card=R.drawable.card_curse)
            else if ("bless" in card.toString())
                Effect(sound=R.raw.tada, card=R.drawable.card_bless)

            // Numbers
            else if ("-2" in card.toString())
                Effect(sound=R.raw.mario_fall, card=R.drawable.card_minus2)
            else if ("-1" in card.toString())
                Effect(sound=R.raw.fnf_death, card=R.drawable.card_minus1)
            else if ("+0" in card.toString())
                Effect(sound=R.raw.stone2, card=R.drawable.card_plus0)
            else if (card.flippy && "+1" in card.toString())
                Effect(sound=R.raw.boom, card=R.drawable.card_plus1_flippy)
            else if ("+1" in card.toString())
                Effect(sound=R.raw.boom, card=R.drawable.card_plus1)
            else if ("+2" in card.toString())
                Effect(sound=R.raw.thwomp, card=R.drawable.card_plus2)
            else if ("x2" in card.toString())
                Effect(sound=R.raw.blast_zone, card=R.drawable.card_x2)

            // Effects
            else if (card.pierce > 0)
                Effect(sound=R.raw.shield_break, card=R.drawable.card_pierce)
            else if (card.muddle)
                Effect(sound=R.raw.hamsterball_dizzy, card=R.drawable.card_muddle)
            else if (card.stun)
                Effect(sound=R.raw.ttyd_timestop, card=R.drawable.card_stun)
            else if (card.extraTarget)
                Effect(sound=R.raw.hamsterball_catapult, card=R.drawable.card_extra_target)
            else// if (card.refresh)
                Effect(sound=R.raw.chest_open, card=R.drawable.card_refresh)

            // Replace sound if it's in disadvantage
            if (card.flippy && currentlyDoingDisadvantage) {
                effectToAdd.sound = R.raw.windows_xp_error
            }

            // Add effect
            effectQueue.add(effectToAdd)

            // Move to bottom row if this is an end
            // If there's only one row, it'll get reset before the next draw anyway
            if (!card.flippy) {
                effectQueue.add(Effect(selectBottomRow = true))
            }

            return card
        }

        override fun addToDrawPile(card: Card) {
            super.addToDrawPile(card)
            effectQueue.add(Effect(sound=R.raw.shuffle))
        }

        override fun addMultipleToDrawPile(cards: Iterable<Card>) {
            super.addMultipleToDrawPile(cards)
            effectQueue.add(Effect(sound=R.raw.shuffle))
        }

        override fun activeCardsToDiscardPile(userDirectlyRequested: Boolean) {
            if (activeCards.size != 0) {
                effectQueue.add(Effect(sound=R.raw.banana_slip))
            }
            super.activeCardsToDiscardPile(userDirectlyRequested)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAdvantage = findViewById<Button>(R.id.btnAdvantage)
        val btnAttack = findViewById<Button>(R.id.btnAttack)
        val btnBless = findViewById<Button>(R.id.btnBless)
        val btnCheat = findViewById<Button>(R.id.btnCheat)
        val btnCurse = findViewById<Button>(R.id.btnCurse)
        val btnDisadvantage = findViewById<Button>(R.id.btnDisadvantage)
        val btnPipis = findViewById<Button>(R.id.btnPipis)
        val btnViewCards = findViewById<Button>(R.id.btnViewCards)

        btnDiscard = findViewById<Button>(R.id.btnDiscard)
        btnRedo = findViewById<Button>(R.id.btnRedo)
        btnSpinny = findViewById<Button>(R.id.btnSpinny)
        btnUndo = findViewById<Button>(R.id.btnUndo)
        llBottomCardRow = findViewById<LinearLayout>(R.id.llBottomCardRow)
        llTopCardRow = findViewById<LinearLayout>(R.id.llTopCardRow)
        selectedCardRow = llTopCardRow
        tvLog = findViewById<TextView>(R.id.tvLog)

        deck = MainActivityDeck()
        deck.addBaseDeck()

        // Adding cards
        btnBless.setOnClickListener {
            startAction(btnBless)
            deck.bless(true)
            endAction(btnBless)
        }

        btnCurse.setOnClickListener {
            startAction(btnCurse)
            deck.curse(true)
            endAction(btnCurse)
        }

        // Viewing cards
        btnCheat.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setMessage("Are you sure you want to view all card states?")
                .setPositiveButton("Yeah lol") { _, _ ->
                    run {
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.setMessage("Draw pile:\n${deck.drawPile}\n\nActive cards:\n${deck.activeCards}\n\nDiscard pile:\n${deck.discardPile}")
                        val alert = dialogBuilder.create()
                        alert.setTitle("Cheat!")
                        alert.show()
                    }
                }

            val alert = dialogBuilder.create()
            alert.setTitle("Cheat?")
            alert.show()
        }

        btnViewCards.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Active cards:\n${deck.activeCards}\n\nDiscard pile:\n${deck.discardPile}")
            val alert = dialogBuilder.create()
            alert.setTitle("Cards")
            alert.show()
        }

        // Undos
        btnUndo.setOnClickListener {
            deck.Undo()
            endAction(btnUndo)
        }

        btnRedo.setOnClickListener {
            deck.Redo()
            endAction(btnRedo)
        }

        // Attacks
        btnAttack.setOnClickListener {
            startAction(btnAttack)
            currentlyDoingDisadvantage = false
            effectQueue.add(Effect(selectTopRow = true, hideBottomRow = true))
            deck.attack()
            endAction(btnAttack)
        }

        btnAdvantage.setOnClickListener {
            startAction(btnAdvantage)
            currentlyDoingDisadvantage = false
            effectQueue.add(Effect(selectTopRow = true, showBottomRow = true))
            deck.advantage()
            endAction(btnAdvantage)
        }

        btnDisadvantage.setOnClickListener {
            startAction(btnDisadvantage)
            currentlyDoingDisadvantage = true
            effectQueue.add(Effect(selectTopRow = true, showBottomRow = true))
            deck.disadvantage()
            endAction(btnDisadvantage)
        }

        btnPipis.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setItems(arrayOf("Inventory", "Enemies", "Power Potion Threshold",
                "HP Danger Threshold", "Go")
            ) { _, which ->
                when (which) {
                    0 -> {}
                    1 -> {}
                    2 -> {}
                    3 -> {}
                    4 -> {}
                }
            }

            val alert = dialogBuilder.create()
            alert.setTitle("Pipis Menu")
            alert.show()
        }

        // Card movement
        btnDiscard.setOnClickListener {
            startAction(btnDiscard)
            deck.activeCardsToDiscardPile(true)
            btnDiscard.isEnabled = false
            endAction(btnDiscard)
        }

        btnSpinny.setOnClickListener {
            startAction(btnSpinny)
            deck.activeCardsToDiscardPile(true)
            deck.discardPileToDrawPile(true)
            btnDiscard.isEnabled = false
            btnSpinny.isEnabled = false
            endAction(btnSpinny)
        }
    }

    fun startAction(button: Button) {
        deck.log("[${button.text.toString().uppercase()}]")
        effectQueue.add(Effect(wipe = true))
    }

    fun endAction(button: Button) {
        // Spinny button?
        if (deck.activeCards.any{it.spinny} || deck.discardPile.any{it.spinny}) {
            btnSpinny.isEnabled = true
        }
        // Discard button?
        if (deck.activeCards.size > 0) {
            btnDiscard.isEnabled = true
        }
        // Undo+Redo buttons?
        btnUndo.isEnabled = deck.undosBack != deck.undoPoints.size - 1
        btnRedo.isEnabled = deck.undosBack != 0
        // Logs
        tvLog.text = deck.getShownLogs().joinToString(separator="\n")
    }
}