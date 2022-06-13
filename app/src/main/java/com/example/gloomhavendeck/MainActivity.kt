package com.example.gloomhavendeck

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties


@RequiresApi(Build.VERSION_CODES.N)
class MainActivity : AppCompatActivity() {
    lateinit var tvLog : TextView
    lateinit var llTopCardRow : LinearLayout
    lateinit var llBottomCardRow : LinearLayout
    lateinit var btnSpinny : Button
    lateinit var btnDiscard : Button
    lateinit var btnUndo : Button
    lateinit var btnRedo : Button

    lateinit var deck : Deck
    lateinit var player : MainActivityPlayer
    var enemies : MutableList<Enemy> = mutableListOf()
    var effectQueue = LinkedList<Effect>()
    lateinit var selectedCardRow : LinearLayout
    var currentlyDoingDisadvantage = false
    val baseEffectSpeed = 1_000/3L
    var effectSpeed = baseEffectSpeed

    inner class Effect(var sound: Int? = null, var card: Int? = null, var wipe: Boolean = false,
                       var selectTopRow: Boolean = false, var selectBottomRow: Boolean = false,
                       var showBottomRow: Boolean = false, var hideBottomRow: Boolean = false) {
        var speed = effectSpeed
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

        inner class UndoPoint : Deck.UndoPoint() {
            var playerJson = Json.encodeToString(player as Player)
            var enemyBlock = enemies.joinToString(separator = "\n") { it.toString() }

            override fun use() {
                super.use()
                // Done like this because inner classes can't be serialized and you can't cast a
                // super into a child class
                // Could be done faster if I manually mapped every field but fuck that lol
                val decodedPlayer = Json.decodeFromString<Player>(playerJson)
                player = MainActivityPlayer() // New one is made because default values aren't serialized
                for (property in Player::class.memberProperties) {
                    try {
                        (property as KMutableProperty<*>).setter.call(player, (property.get(decodedPlayer))
                    )}
                    catch (e: Exception)
                    {}
                }
                // Enemies
                enemies = Enemy.createMany(enemyBlock, player.scenarioLevel).toMutableList()
            }
        }

        override fun getUndoPoint(): Deck.UndoPoint {
            return UndoPoint()
        }

        val effectLoop = Thread {
            while (true) {
                if (effectQueue.size > 0) {
                    val effect = effectQueue.remove()
                    effect.run()
                    if (effect.sound != null) {
                        Thread.sleep(effect.speed)
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

        override fun attack(basePower: Int, userDirectlyRequested: Boolean) : Card {
            currentlyDoingDisadvantage = false
            effectQueue.add(Effect(selectTopRow = true, hideBottomRow = true, wipe=true))
            return super.attack(basePower, userDirectlyRequested)
        }

        override fun advantage(basePower: Int, userDirectlyRequested: Boolean) : Card {
            currentlyDoingDisadvantage = false
            effectQueue.add(Effect(selectTopRow = true, showBottomRow = true, wipe=true))
            return super.advantage(basePower, userDirectlyRequested)
        }

        override fun disadvantage(basePower: Int, userDirectlyRequested: Boolean) : Card {
            currentlyDoingDisadvantage = true
            effectQueue.add(Effect(selectTopRow = true, showBottomRow = true, wipe=true))
            return super.disadvantage(basePower, userDirectlyRequested)
        }
    }

    inner class MainActivityPlayer() : Player() {
        override fun useItem(item: Item) {
            super.useItem(item)
            effectQueue.add(Effect(card = item.graphic, sound = item.sound, selectTopRow = true))
            deck.log("Used a $item")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n")
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

        player = MainActivityPlayer()
        enemies = Enemy.createMany("""Dog1 12
2 8
3 15,shield 1
4 4
vermling scout 7: 1 2 3 e5 6""", player.scenarioLevel).toMutableList()
        // Because the deck has player undos it needs to be made after
        deck = MainActivityDeck()
        deck.addBaseDeck()

            //Enemy("Dog 2 g")
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
            deck.attack(userDirectlyRequested=true)
            endAction(btnAttack)
        }

        btnAdvantage.setOnClickListener {
            startAction(btnAdvantage)
            deck.advantage(userDirectlyRequested=true)
            endAction(btnAdvantage)
        }

        btnDisadvantage.setOnClickListener {
            startAction(btnDisadvantage)
            deck.disadvantage(userDirectlyRequested=true)
            endAction(btnDisadvantage)
        }

        btnPipis.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setItems(arrayOf(
                "Inventory (Currently ${player.usableItems.size}/${player.usableItems.size+player.unusableItems.size})",
                "Enemies",
                "Enemy Menu",
                "HP (Currently ${player.hp})",
                "Statuses (Currently ${player.statuses})",
                "Power Potion Threshold (Currently ${player.powerPotionThreshold})",
                "HP Danger Threshold (Currently ${player.hpDangerThreshold})",
                "Pierce (Currently ${player.pierce})",
                "Scenario Level (Currently ${player.scenarioLevel})",
                "Go")
            ) { _, which ->
                when (which) {
                    // Inventory
                    0 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Usable Items?")
                        val scrollView = ScrollView(this)
                        val linearLayout = LinearLayout(this)
                        scrollView.addView(linearLayout)
                        linearLayout.orientation = LinearLayout.VERTICAL
                        for (item in (player.usableItems + player.unusableItems).sortedBy { it.name }) {
                            if (item.permanent) {
                                continue
                            }
                            val checkBox = CheckBox(this)
                            checkBox.setText(item.name)
                            checkBox.isChecked = item in player.usableItems
                            checkBox.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                if (on) {
                                    player.usableItems.add(item)
                                    player.unusableItems.remove(item)
                                } else {
                                    player.usableItems.remove(item)
                                    player.unusableItems.add(item)
                                }
                                deck.log("Updated items.")
                                deck.addUndoPoint()
                                endAction(btnPipis)
                            }
                            linearLayout.addView(checkBox)
                        }
                        alert.setView(scrollView)
                        alert.setPositiveButton("Done") {_,_ ->}
                        alert.show()
                    }
                    // Enemies
                    1 -> {
                        // This will eventually stack overflow if the user is sufficiently stupid but whatever lol
                        var text = enemies.joinToString(separator = "\n") { it.toString() }
                        var title = "New enemies?"
                        fun showAlert() {
                            val alert = AlertDialog.Builder(this)
                            alert.setTitle(title)
                            val input = EditText(this)
                            input.setText(text)
                            alert.setView(input)
                            alert.setPositiveButton("Set") { _, _ ->
                                try {
                                    text = input.text.toString()
                                    enemies = Enemy.createMany(text, player.scenarioLevel).toMutableList()
                                    text = enemies.joinToString(separator = "\n") { it.toString() }
                                    title = "Result:"
                                } catch (e: Exception) {
                                    title = e.message.toString()
                                }
                                showAlert()
                                deck.log("Updated enemies.")
                                deck.addUndoPoint()
                                endAction(btnPipis)
                            }
                            alert.show()
                        }
                        showAlert()
                    }
                    // Enemy menu
                    2 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("Enemy Menu")
                        val scrollView = ScrollView(this)
                        val llRows = LinearLayout(this)
                        scrollView.addView(llRows)
                        llRows.orientation = LinearLayout.VERTICAL
                        val textSize = 8f
                        for (enemy in enemies.sortedBy { it.name }) {
                            val llRow = LinearLayout(this)
                            llRow.orientation = LinearLayout.HORIZONTAL
                            llRows.addView(llRow)
                            // Name
                            val tvName = TextView(this)
                            tvName.textSize = textSize
                            tvName.text = enemy.name
                            llRow.addView(tvName)
                            // Taken
                            val npTaken = NumberPicker(this)
                            npTaken.minValue = 0
                            npTaken.maxValue = enemy.maxHp
                            npTaken.value = enemy.taken
                            npTaken.layoutParams = ViewGroup.LayoutParams(300,100)
                            npTaken.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                                enemy.taken = new
                            }
                            llRow.addView(npTaken)
                            // Targeted checkbox
                            val cbTargeted = CheckBox(this)
                            cbTargeted.textSize = textSize
                            cbTargeted.text = "Trgt"
                            cbTargeted.isChecked = enemy.targeted
                            cbTargeted.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                enemy.targeted = !enemy.targeted
                            }
                            llRow.addView(cbTargeted)
                            // Ballista checkbox
                            val cbBallista = CheckBox(this)
                            cbBallista.textSize = textSize
                            cbBallista.text = "Blst"
                            cbBallista.isChecked = enemy.inBallistaRange
                            cbBallista.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                enemy.inBallistaRange = !enemy.inBallistaRange
                            }
                            llRow.addView(cbBallista)
                            // Extra Target checkbox
                            val cbExtraTarget = CheckBox(this)
                            cbExtraTarget.textSize = textSize
                            cbExtraTarget.text = "ExTr"
                            cbExtraTarget.isChecked = enemy.extraTarget
                            cbExtraTarget.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                enemy.extraTarget = !enemy.extraTarget
                            }
                            llRow.addView(cbExtraTarget)
                            // Melee Range checkbox
                            val cbMeleeRange = CheckBox(this)
                            cbMeleeRange.textSize = textSize
                            cbMeleeRange.text = "MlRn"
                            cbMeleeRange.isChecked = enemy.inMeleeRange
                            cbMeleeRange.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                enemy.inMeleeRange = !enemy.inMeleeRange
                            }
                            llRow.addView(cbMeleeRange)
                            // Retaliate Range checkbox
                            val cbRetaliateRange = CheckBox(this)
                            cbRetaliateRange.textSize = textSize
                            cbRetaliateRange.text = "RtRn"
                            cbRetaliateRange.isChecked = enemy.inRetaliateRange
                            cbRetaliateRange.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                enemy.inRetaliateRange = !enemy.inRetaliateRange
                            }
                            llRow.addView(cbRetaliateRange)
                        }
                        alert.setView(scrollView)
                        alert.setOnDismissListener{
                            deck.log("Updated enemies via menu.")
                            deck.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // HP
                    3 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New HP?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(player.hp.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            player.hp = input.text.toString().toInt()
                            deck.log("Updated HP.")
                            deck.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Statuses
                    4 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Statuses?")
                        val scrollView = ScrollView(this)
                        val linearLayout = LinearLayout(this)
                        scrollView.addView(linearLayout)
                        linearLayout.orientation = LinearLayout.VERTICAL
                        for (status in Status.values()) {
                            val checkBox = CheckBox(this)
                            checkBox.setText(status.name)
                            checkBox.isChecked = status in player.statuses
                            checkBox.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                if (on) {
                                    player.statuses.add(status)
                                    deck.log("Updated statuses.")
                                    deck.addUndoPoint()
                                    endAction(btnPipis)
                                } else {
                                    player.statuses.remove(status)
                                    deck.log("Updated statuses.")
                                    deck.addUndoPoint()
                                    endAction(btnPipis)
                                }
                            }
                            linearLayout.addView(checkBox)
                        }
                        alert.setView(scrollView)
                        alert.setPositiveButton("Done") {_,_ ->}
                        alert.show()
                    }
                    // Power Potion Threshold
                    5 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Power Potion Threshold?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(player.powerPotionThreshold.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            player.powerPotionThreshold = input.text.toString().toInt()
                            deck.log("Updated power pot threshold.")
                            deck.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // HP Danger Threshold
                    6 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New HP Danger Threshold?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(player.hpDangerThreshold.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            player.hpDangerThreshold = input.text.toString().toInt()
                            deck.log("Updated HP danger threshold.")
                            deck.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Pierce
                    7 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Pierce?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(player.pierce.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            player.pierce = input.text.toString().toInt()
                            deck.log("Updated pierce.")
                            deck.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Scenario Level
                    8 -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Scenario Level?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(player.scenarioLevel.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            player.scenarioLevel = input.text.toString().toInt()
                            deck.log("Updated scenario level.")
                            deck.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Go
                    9 -> {
                        effectSpeed = 1_000/2L
                        deck.pipis(player, enemies)
                        effectSpeed = baseEffectSpeed
                        endAction(btnPipis)
                    }
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
        deck.log("")
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