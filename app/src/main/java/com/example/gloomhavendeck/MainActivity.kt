package com.example.gloomhavendeck

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.size
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    lateinit var tvLog : TextView
    lateinit var llTopCardRow : LinearLayout
    lateinit var llBottomCardRow : LinearLayout
    lateinit var llItemRow : LinearLayout
    lateinit var btnSpinny : Button
    lateinit var btnDiscard : Button
    lateinit var btnUndo : Button
    lateinit var btnRedo : Button

    lateinit var controller : MainActivityController
    var enemyOrder: MutableList<String> = mutableListOf()
    var effectQueue = LinkedList<Effect>()
    lateinit var selectedCardRow : LinearLayout
    var currentlyDoingDisadvantage = false
    val baseEffectSpeed = 1_000/3L
    var effectSpeed = baseEffectSpeed

    lateinit var discardAnim: ObjectAnimator
    lateinit var spinnyAnim: ObjectAnimator

    inner class Effect(var sound: SoundBundle? = null, var card: Int? = null, var wipe: Boolean = false,
                       var selectTopRow: Boolean = false, var selectBottomRow: Boolean = false,
                       var showBottomRow: Boolean = false, var hideBottomRow: Boolean = false,
                       var showItemRow: Boolean = false, var hideItemRow: Boolean = false,
                       var newItemRowDisplay: List<Boolean>? = null) {
        var speed = effectSpeed
        @SuppressLint("UseCompatLoadingForDrawables")
        fun run() {
            if (sound != null) {
                val mediaPlayer: MediaPlayer = MediaPlayer.create(this@MainActivity, sound!!.getSound() as Int)
                mediaPlayer.start()
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
                    llBottomCardRow.visibility = View.VISIBLE
                }
            }
            if (hideBottomRow) {
                runOnUiThread {
                    llBottomCardRow.visibility = View.GONE
                }
            }
            
            if (showItemRow) {
                runOnUiThread {
                    llItemRow.visibility = View.VISIBLE
                }
            }
            if (hideItemRow) {
                runOnUiThread {
                    llItemRow.visibility = View.GONE
                }
            }
            if (newItemRowDisplay != null) {
                runOnUiThread {
                    llItemRow.removeAllViews()
                }
                for ((i, item) in (controller.player.inventory.usableItems + controller.player.inventory.unusableItems).sortedBy { it.name }.withIndex()) {
                    val imageView = item.getImageView(this@MainActivity, newItemRowDisplay!![i])
                    runOnUiThread {
                        llItemRow.addView(imageView)
                    }
                }
            }
        }
    }

    fun sortEnemies() {
        val nameRegex = Regex("[a-z]+", RegexOption.IGNORE_CASE)
        controller.enemies = controller.enemies.sortedBy { it.name }.toMutableList()
        controller.enemies = controller.enemies.sortedBy {
            val name = nameRegex.find(it.name)!!.value
            if (name in enemyOrder) {
                enemyOrder.indexOf(name)
            } else {
                -1
            }
        }.toMutableList()
    }

    inner class MainActivityController(filesDir: String) : Controller(filesDir) {
        override fun getUndoPoint(): UndoPoint {
            return MainActivityUndoPoint(this)
        }
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

    inner class MainActivityDeck(controller: Controller) : Deck(controller) {
        override fun drawSingleCard(): Card {
            val card = super.drawSingleCard()
            val effectToAdd : Effect

            // Named
            effectToAdd = if ("null" in card.toString())
                Effect(sound=SoundBundle.NULL, card=R.drawable.card_null)
            else if ("curse" in card.toString())
                Effect(sound=SoundBundle.CURSE, card=R.drawable.card_curse)
            else if ("bless" in card.toString())
                Effect(sound=SoundBundle.BLESS, card=R.drawable.card_bless)


            // Effects
            else if (card.pierce > 0)
                Effect(sound=SoundBundle.PIERCE, card=R.drawable.card_pierce)
            else if ("+3" in card.toString() && card.muddle)
                Effect(sound=SoundBundle.MUDDLE, card=R.drawable.card_plus3muddle)
            else if (card.muddle)
                Effect(sound=SoundBundle.MUDDLE, card=R.drawable.card_muddle)
            else if (card.stun)
                Effect(sound=SoundBundle.STUN, card=R.drawable.card_stun)
            else if (card.extraTarget)
                Effect(sound=SoundBundle.EXTRATARGET, card=R.drawable.card_extra_target)
            else if (card.healAlly > 0)
                Effect(sound=SoundBundle.HEAL, card=R.drawable.card_plus1healally)
            else if (card.shieldSelf > 0)
                Effect(sound=SoundBundle.SHIELD, card=R.drawable.card_plus3shield)
            else if (card.element == Element.DARK)
                Effect(sound=SoundBundle.DARK, card=R.drawable.card_plus2dark)
            else if (card.regenerate)
                Effect(sound=SoundBundle.REGENERATE, card=R.drawable.card_plus2regenerate)
            else if (card.curse)
                Effect(sound=SoundBundle.CURSE, card=R.drawable.card_plus2curse)

            // Numbers
            else if ("-2" in card.toString())
                Effect(sound=SoundBundle.MINUS2, card=R.drawable.card_minus2)
            else if ("-1" in card.toString())
                Effect(sound=SoundBundle.MINUS1, card=R.drawable.card_minus1)
            else if ("+0" in card.toString())
                Effect(sound=SoundBundle.DEFAULT, card=R.drawable.card_plus0)
            else if (card.flippy && "+1" in card.toString())
                Effect(sound=SoundBundle.PLUS1, card=R.drawable.card_plus1_flippy)
            else if ("+1" in card.toString())
                Effect(sound=SoundBundle.PLUS1, card=R.drawable.card_plus1)
            else if ("+2" in card.toString())
                Effect(sound=SoundBundle.PLUS2, card=R.drawable.card_plus2)
            else// if ("x2" in card.toString())
                Effect(sound=SoundBundle.X2, card=R.drawable.card_x2)

            // Replace sound if it's in disadvantage
            if (card.flippy && currentlyDoingDisadvantage) {
                effectToAdd.sound = SoundBundle.DISADVANTAGE
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
            effectQueue.add(Effect(sound=SoundBundle.SHUFFLE))
        }

        override fun addMultipleToDrawPile(cards: Iterable<Card>) {
            super.addMultipleToDrawPile(cards)
            effectQueue.add(Effect(sound=SoundBundle.SHUFFLE))
        }

        override fun activeCardsToDiscardPile(userDirectlyRequested: Boolean) {
            if (activeCards.size != 0) {
                effectQueue.add(Effect(sound=SoundBundle.DISCARD))
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

    inner class MainActivityUndoPoint(controller: Controller) : UndoPoint(controller) {
        var playerJson = Json.encodeToString(controller.player as Player)
        var enemyBlock = controller.enemies.joinToString(separator = "\n") { it.toString() }

        override fun use(controller: Controller) {
            super.use(controller)
            // Done like this because inner classes can't be serialized and you can't cast a
            // super into a child class
            // Could be done faster if I manually mapped every field but fuck that lol

            // Player
            val decodedPlayer = Json.decodeFromString<Player>(playerJson)
            controller.player = MainActivityPlayer() // New one is made because default values aren't serialized
            for (property in Player::class.memberProperties) {
                try {
                    (property as KMutableProperty<*>).setter.call(controller.player, (property.get(decodedPlayer))
                    )}
                catch (e: Exception)
                {}
            }
            // Inventory
            val decodedInventory = controller.player.inventory
            controller.player.inventory = MainActivityInventory() // New one is made because default values aren't serialized
            for (property in Inventory::class.memberProperties) {
                try {
                    (property as KMutableProperty<*>).setter.call(controller.player.inventory, (property.get(decodedInventory))
                    )}
                catch (e: Exception)
                {}
            }
            // Deck
            val decodedDeck = controller.deck
            controller.deck = MainActivityDeck(controller) // New one is made because default values aren't serialized
            for (property in Deck::class.memberProperties) {
                try {
                    (property as KMutableProperty<*>).setter.call(controller.deck, (property.get(decodedDeck))
                    )}
                catch (e: Exception)
                {}
            }
            // Enemies
            controller.enemies = Enemy.createMany(enemyBlock, controller.player.scenarioLevel).toMutableList()
        }
    }

    inner class MainActivityInventory() : Inventory() {
        override fun regainItem(item: Item) {
            super.regainItem(item)
            displayChangedInventory()
        }
        override fun loseItem(item: Item) {
            super.loseItem(item)
            displayChangedInventory()
        }
        fun displayChangedInventory() {
            if (llItemRow.isVisible) {
                val newItemRowDisplay = mutableListOf<Boolean>()
                for (item in (controller.player.inventory.usableItems + controller.player.inventory.unusableItems).sortedBy { it.name }) {
                    newItemRowDisplay.add(item in controller.player.inventory.usableItems)
                }
                effectQueue.add(Effect(newItemRowDisplay = newItemRowDisplay))
            }
        }
    }

    inner class MainActivityPlayer() : Player() {
        init {
            inventory = MainActivityInventory()
        }
        override fun useItem(item: Item, deck: Deck, viaPipis: Boolean) {
            effectQueue.add(Effect(card = item.graphic, sound = item.sound, selectTopRow = true))
            controller.log("Using a $item...")
            controller.logIndent += 1
            super.useItem(item, controller.deck, viaPipis)
            controller.logIndent -= 1
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
        val btnSimplify = findViewById<Button>(R.id.btnSimplify)
        val btnManage = findViewById<Button>(R.id.btnManage)

        controller = MainActivityController(applicationContext.filesDir.canonicalPath)
        runOnUiThread {
            val loadBuilder = AlertDialog.Builder(this@MainActivity)
            loadBuilder.setPositiveButton("Yeah") { dialog, which ->
                val decodedController = Json.decodeFromString<Controller>(File(Paths.get(applicationContext.filesDir.canonicalPath, "current_state.json").toString()).readText())
                for (property in Controller::class.memberProperties) {
                    try {
                        (property as KMutableProperty<*>).setter.call(controller, (property.get(decodedController))
                        )}
                    catch (e: Exception)
                    {Log.d("heyyyyy", e.message.toString())}
                }
                controller.InsertSelfIntoAllChildren()
                // Player
                val decodedPlayer = controller.player
                controller.player = MainActivityPlayer() // New one is made because default values aren't serialized
                for (property in Player::class.memberProperties) {
                    try {
                        (property as KMutableProperty<*>).setter.call(controller.player, (property.get(decodedPlayer))
                        )}
                    catch (e: Exception)
                    {}
                }
                // Inventory
                val decodedInventory = controller.player.inventory
                controller.player.inventory = MainActivityInventory() // New one is made because default values aren't serialized
                for (property in Inventory::class.memberProperties) {
                    try {
                        (property as KMutableProperty<*>).setter.call(controller.player.inventory, (property.get(decodedInventory))
                        )}
                    catch (e: Exception)
                    {}
                }
                // Deck
                val decodedDeck = controller.deck
                controller.deck = MainActivityDeck(controller) // New one is made because default values aren't serialized
                for (property in Deck::class.memberProperties) {
                    try {
                        (property as KMutableProperty<*>).setter.call(controller.deck, (property.get(decodedDeck))
                        )}
                    catch (e: Exception)
                    {}
                }
            }
            loadBuilder.setNegativeButton("No") { _, _->

                controller.player = MainActivityPlayer()
                controller.enemies = Enemy.createMany("""Dog1 12
2 8
3 15,shield 1
4 4
vermling scout 7: 1 2 3 n5 6""", controller.player.scenarioLevel).toMutableList()
                // Because the controller.deck has controller.player undos it needs to be made after
                controller.deck = MainActivityDeck(controller)
                val deckBuilder = AlertDialog.Builder(this@MainActivity)
                deckBuilder.setPositiveButton("Three Spears") { _, _ ->
                    controller.deck.addBaseDeckThreeSpears()
                }
                deckBuilder.setNegativeButton("Eye") { _, _ ->
                    controller.deck.addBaseDeckEye()
                }
                deckBuilder.setTitle("Class?")
                deckBuilder.show()
            }
            loadBuilder.setTitle("Load?")
            loadBuilder.show()
        }

        btnDiscard = findViewById<Button>(R.id.btnDiscard)
        btnRedo = findViewById<Button>(R.id.btnRedo)
        btnSpinny = findViewById<Button>(R.id.btnSpinny)
        btnUndo = findViewById<Button>(R.id.btnUndo)
        llBottomCardRow = findViewById<LinearLayout>(R.id.llBottomCardRow)
        llTopCardRow = findViewById<LinearLayout>(R.id.llTopCardRow)
        llItemRow = findViewById<LinearLayout>(R.id.llItemRow)
        selectedCardRow = llTopCardRow
        tvLog = findViewById<TextView>(R.id.tvLog)

        discardAnim = ObjectAnimator.ofInt(
            btnDiscard,
            "textColor",
            Color.RED,
            Color.parseColor("#434646")
        )
        spinnyAnim = ObjectAnimator.ofInt(
            btnSpinny,
            "textColor",
            Color.RED,
            Color.parseColor("#434646")
        )
        for (anim in listOf(discardAnim, spinnyAnim))
        {
            anim.duration = 500
            anim.setEvaluator(ArgbEvaluator())
            anim.repeatCount = ValueAnimator.INFINITE
            anim.repeatMode = ValueAnimator.REVERSE
        }

            //Enemy("Dog 2 g")
        // Adding cards
        btnBless.setOnClickListener {
            buttonBehavior(btnBless) {
                controller.deck.bless(true)
            }
        }

        btnCurse.setOnClickListener {
            buttonBehavior(btnCurse) {
                controller.deck.curse(true)
            }
        }

        // Viewing cards
        btnCheat.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setMessage("Are you sure you want to view all card states?")
                .setPositiveButton("Yeah lol") { _, _ ->
                    run {
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.setMessage("Draw pile:\n${controller.deck.drawPile}\n\nActive cards:\n${controller.deck.activeCards}\n\nDiscard pile:\n${controller.deck.discardPile}")
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
            dialogBuilder.setMessage("Active cards:\n${controller.deck.activeCards}\n\nDiscard pile:\n${controller.deck.discardPile}")
            val alert = dialogBuilder.create()
            alert.setTitle("Cards")
            alert.show()
        }

        // Undos
        btnUndo.setOnClickListener {
            controller.Undo()
            endAction(btnUndo)
        }

        btnRedo.setOnClickListener {
            controller.Redo()
            endAction(btnRedo)
        }

        // Attacks
        btnAttack.setOnClickListener {
            buttonBehavior(btnAttack) {
                effectQueue.add(Effect(hideItemRow=true))
                controller.deck.attack(userDirectlyRequested = true)
            }
        }

        btnAdvantage.setOnClickListener {
            buttonBehavior(btnAdvantage) {
                effectQueue.add(Effect(hideItemRow=true))
                controller.deck.advantage(userDirectlyRequested = true)
            }
        }

        btnDisadvantage.setOnClickListener {
            buttonBehavior(btnDisadvantage) {
                effectQueue.add(Effect(hideItemRow=true))
                controller.deck.disadvantage(userDirectlyRequested = true)
            }
        }

        btnPipis.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)

            var i = 0
            dialogBuilder.setItems(arrayOf(
                "Inventory (Currently ${controller.player.inventory.usableItems.size}/${controller.player.inventory.usableItems.size+controller.player.inventory.unusableItems.size})",
                "Enemies",
                "Enemy Order",
                "Enemy Menu",
                "HP (Currently ${controller.player.hp})",
                "Power Potion Threshold (Currently ${controller.player.powerPotionThreshold})",
                "HP Danger Threshold (Currently ${controller.player.hpDangerThreshold})",
                "Pierce (Currently ${controller.player.pierce})",
                "Scenario Level (Currently ${controller.player.scenarioLevel})",
                "Discard Status (Currently ${controller.player.discardedCards})",
                "Go")
            ) { _, which ->
                when (which) {
                    // Inventory
                    i++ -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Usable Items?")
                        val scrollView = ScrollView(this)
                        val linearLayout = LinearLayout(this)
                        scrollView.addView(linearLayout)
                        linearLayout.orientation = LinearLayout.VERTICAL
                        for (item in (controller.player.inventory.usableItems + controller.player.inventory.unusableItems).sortedBy { it.name }) {
                            if (item.permanent) {
                                continue
                            }
                            val checkBox = CheckBox(this)
                            checkBox.setText(item.name)
                            checkBox.isChecked = item in controller.player.inventory.usableItems
                            checkBox.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                if (on) {
                                    controller.player.inventory.regainItem(item)
                                } else {
                                    controller.player.inventory.loseItem(item)
                                }
                                controller.log("Updated items.")
                                controller.addUndoPoint()
                                endAction(btnPipis)
                            }
                            linearLayout.addView(checkBox)
                        }
                        alert.setView(scrollView)
                        alert.setPositiveButton("Done") {_,_ ->}
                        alert.show()
                    }
                    // Enemies
                    i++ -> {
                        // This will eventually stack overflow if the user is sufficiently stupid but whatever lol
                        var text = controller.enemies.joinToString(separator = "\n") { it.toString() }
                        var title = "New controller.enemies?"
                        fun showAlert() {
                            val alert = AlertDialog.Builder(this)
                            alert.setTitle(title)
                            val input = EditText(this)
                            input.setText(text)
                            alert.setView(input)
                            alert.setPositiveButton("Set") { _, _ ->
                                try {
                                    text = input.text.toString()
                                    controller.enemies = Enemy.createMany(text, controller.player.scenarioLevel).toMutableList()
                                    text = controller.enemies.joinToString(separator = "\n") { it.toString() }
                                    title = "Result:"
                                } catch (e: Exception) {
                                    title = e.message.toString()
                                }
                                showAlert()
                                sortEnemies()
                                controller.log("Updated controller.enemies.")
                                controller.addUndoPoint()
                                endAction(btnPipis)
                            }
                            alert.show()
                        }
                        showAlert()
                    }
                    // Enemy order
                    i++ -> {
                        val nameRegex = Regex("[a-z]+", RegexOption.IGNORE_CASE)
                        val enemyNames = HashSet<String>()
                        for (enemy in controller.enemies) {
                            enemyNames.add(nameRegex.find(enemy.name)!!.value)
                        }
                        enemyOrder = mutableListOf()
                        // This will eventually stack overflow if the user is sufficiently stupid but whatever lol
                        fun showAlert() {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("ORDER??????????")
                            val alert = builder.create()
                            val scrollView = ScrollView(this)
                            val llRows = LinearLayout(this)
                            scrollView.addView(llRows)
                            llRows.orientation = LinearLayout.VERTICAL
                            for (enemyName in enemyNames.sorted()) {
                                // Name
                                val btnName = Button(this)
                                btnName.text = enemyName
                                btnName.setOnClickListener {
                                    enemyNames.remove(enemyName)
                                    enemyOrder.add(enemyName)
                                    if (enemyNames.size > 0) {
                                        showAlert()
                                    } else {
                                        controller.log("Updated enemy order.")
                                        sortEnemies()
                                        controller.addUndoPoint()
                                        endAction(btnPipis)
                                    }
                                    alert.cancel()
                                }
                                llRows.addView(btnName)
                            }
                            alert.setView(scrollView)
                            alert.show()
                        }
                        showAlert()
                    }
                    // Enemy menu
                    i++ -> {
                        val alert = AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar)
                        alert.setTitle("Enemy Menu")
                        val scrollView = ScrollView(this)
                        val llRows = LinearLayout(this)
                        scrollView.addView(llRows)
                        llRows.orientation = LinearLayout.VERTICAL
                        val textSize = 8f
                        for (enemy in controller.enemies) {
                            val llRow = LinearLayout(this)
                            llRow.orientation = LinearLayout.HORIZONTAL
                            llRow.gravity = Gravity.CENTER_VERTICAL
                            llRows.addView(llRow)
                            // Update change display, and also create the stuff that needs to be there in advance
                            val btnPlus1 = Button(this)
                            val btnPlus5 = Button(this)
                            val npTaken = NumberPicker(this)
                            val tvChangeDisplay = TextView(this)
                            val startingTaken = enemy.taken
                            var takenChange = 0
                            fun changeTakenTo(taken: Int) {
                                if (takenChange != taken-startingTaken) {
                                    takenChange = taken-startingTaken
                                    enemy.taken = taken
                                    npTaken.value = taken
                                    tvChangeDisplay.text = takenChange.toString()
                                    if (takenChange > 0) {
                                        tvChangeDisplay.setTextColor(Color.GREEN)
                                    }
                                    else if (takenChange < 0) {
                                        tvChangeDisplay.setTextColor(Color.RED)
                                    } else {
                                        tvChangeDisplay.setTextColor(Color.BLACK)
                                    }
                                }
                            }
                            // Name
                            val tvName = TextView(this)
                            tvName.textSize = textSize
                            tvName.text = enemy.name
                            llRow.addView(tvName)
                            // Taken
                            npTaken.minValue = 0
                            npTaken.maxValue = enemy.maxHp
                            npTaken.value = enemy.taken
                            npTaken.gravity = Gravity.LEFT
                            npTaken.layoutParams = ViewGroup.LayoutParams(300,200)
                            npTaken.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                                changeTakenTo(new)
                            }
                            llRow.addView(npTaken)
                            // This bit has two rows
                            val llRowRow = LinearLayout(this)
                            llRowRow.orientation = LinearLayout.VERTICAL
                            llRowRow.gravity = Gravity.CENTER_VERTICAL
                            llRow.addView(llRowRow)
                            // +1
                            btnPlus1.text = "+1"
                            btnPlus1.layoutParams = ViewGroup.LayoutParams(200,100)
                            btnPlus1.setOnClickListener() {
                                changeTakenTo(enemy.taken+1)
                            }
                            llRowRow.addView(btnPlus1)
                            // +5
                            btnPlus5.text = "+5"
                            btnPlus5.layoutParams = ViewGroup.LayoutParams(200,100)
                            btnPlus5.setOnClickListener() {
                                changeTakenTo(enemy.taken+5)
                            }
                            llRowRow.addView(btnPlus5)
                            // Display change
                            llRowRow.addView(tvChangeDisplay)
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
                            // Poisoned checkbox
                            val cbPoisoned = CheckBox(this)
                            cbPoisoned.textSize = textSize
                            cbPoisoned.text = "Poisn"
                            cbPoisoned.isChecked = enemy.poisoned
                            cbPoisoned.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                enemy.poisoned = !enemy.poisoned
                            }
                            llRow.addView(cbPoisoned)
                        }
                        alert.setView(scrollView)
                        alert.setOnDismissListener{
                            controller.log("Updated controller.enemies via menu.")
                            controller.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // HP
                    i++ -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New HP?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(controller.player.hp.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            controller.player.hp = input.text.toString().toInt()
                            controller.log("Updated HP.")
                            controller.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Power Potion Threshold
                    i++ -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Power Potion Threshold?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(controller.player.powerPotionThreshold.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            controller.player.powerPotionThreshold = input.text.toString().toInt()
                            controller.log("Updated power pot threshold.")
                            controller.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // HP Danger Threshold
                    i++ -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New HP Danger Threshold?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(controller.player.hpDangerThreshold.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            controller.player.hpDangerThreshold = input.text.toString().toInt()
                            controller.log("Updated HP danger threshold.")
                            controller.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Pierce
                    i++ -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Pierce?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(controller.player.pierce.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            controller.player.pierce = input.text.toString().toInt()
                            controller.log("Updated pierce.")
                            controller.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Scenario Level
                    i++ -> {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("New Scenario Level?")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_NUMBER
                        input.setRawInputType(Configuration.KEYBOARD_12KEY)
                        input.setText(controller.player.scenarioLevel.toString())
                        alert.setView(input)
                        alert.setPositiveButton("Set") { _, _ ->
                            controller.player.scenarioLevel = input.text.toString().toInt()
                            controller.log("Updated scenario level.")
                            controller.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Discard
                    i++ -> {
                        // Alert
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle("Discard status?")
                        // SV
                        val scrollView = ScrollView(this)
                        alert.setView(scrollView)
                        // LL
                        val linearLayout = LinearLayout(this)
                        linearLayout.orientation = LinearLayout.VERTICAL
                        scrollView.addView(linearLayout)
                        // Make them, since they reference each other
                        val npDiscarded = NumberPicker(this)
                        val cbPipis = CheckBox(this)
                        val cbBallista = CheckBox(this)
                        // Discard spinner
                        npDiscarded.minValue = 0
                        npDiscarded.maxValue = 9
                        npDiscarded.value = controller.player.discardedCards
                        npDiscarded.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                            controller.player.discardedCards = new
                            cbBallista.isChecked = controller.player.discardedBallista
                            cbPipis.isChecked = controller.player.discardedPipis
                        }
                        linearLayout.addView(npDiscarded)
                        // Targeted checkbox
                        cbPipis.text = "Discarded Pipis"
                        cbPipis.isChecked = controller.player.discardedPipis
                        cbPipis.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                            controller.player.discardedPipis = on
                            npDiscarded.value = controller.player.discardedCards
                        }
                        linearLayout.addView(cbPipis)
                        // Ballista checkbox
                        cbBallista.text = "Discarded Ballista"
                        cbBallista.isChecked = controller.player.discardedBallista
                        cbBallista.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                            controller.player.discardedBallista = on
                            npDiscarded.value = controller.player.discardedCards
                        }
                        linearLayout.addView(cbBallista)
                        alert.setOnDismissListener{
                            controller.log("Updated discard status.")
                            controller.addUndoPoint()
                            endAction(btnPipis)
                        }
                        alert.show()
                    }
                    // Go
                    i++ -> {
                        effectQueue.add(Effect(showItemRow=true))
                        (controller.player.inventory as MainActivityInventory).displayChangedInventory()
                        effectSpeed = 1_000/4L
                        controller.deck.pipis(controller.player, controller.enemies, this)
                        effectSpeed = baseEffectSpeed
                        simplifyTheGamestate()
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
            buttonBehavior(btnDiscard) {
                controller.deck.activeCardsToDiscardPile(true)
                btnDiscard.isEnabled = false
                discardAnim.end()
            }
        }

        btnSpinny.setOnClickListener {
            buttonBehavior(btnSpinny) {
                controller.deck.activeCardsToDiscardPile(true)
                controller.deck.discardPileToDrawPile(true)
                btnDiscard.isEnabled = false
                btnSpinny.isEnabled = false
                discardAnim.end()
                spinnyAnim.end()
            }
        }

        // Hi
        btnSimplify.setOnClickListener {
            buttonBehavior(btnSimplify) {
                simplifyTheGamestate()
                controller.addUndoPoint()
            }
        }

        btnManage.setOnClickListener {
            buttonBehavior(btnManage) {
                class CustomDialogClass(var context: Activity, theme: Int) : Dialog(context, theme) {
                    fun displayItems() {
                        val llItemContainer = findViewById<LinearLayout>(R.id.llItemContainer)!!
                        llItemContainer.removeAllViews()
                        var row = LinearLayout(context)
                        for ((n, item) in (controller.player.inventory.usableItems + controller.player.inventory.unusableItems).sorted().withIndex()) {
                            if (n % 3 == 0) {
                                llItemContainer.addView(row)
                                row = LinearLayout(context)
                                val params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    1.0f
                                )
                                row.layoutParams = params
                            }
                            val imageView = item.getImageView(context, controller.player.inventory.usableItems.contains(item))

                            if (!item.permanent) {
                                // Click
                                imageView.setOnClickListener() {
                                    try {
                                        if (controller.player.inventory.usableItems.contains(item)) {
                                            controller.player.useItem(item, controller.deck, false)
                                        } else {
                                            controller.player.inventory.regainItem(item)
                                        }
                                        setUpEverything()
                                    } catch (e: Exception) {

                                    }
                                }
                                // Long Click
                                if (controller.player.inventory.usableItems.contains(item)) {
                                    imageView.setOnLongClickListener() {
                                        controller.player.inventory.loseItem(item)
                                        displayItems()
                                        true
                                    }
                                }
                            }

                            row.addView(imageView)
                        }
                        llItemContainer.addView(row)
                    }

                    fun setUpEverything() {
                        val llStatsContainer = findViewById<LinearLayout>(R.id.llStatsContainer)!!
                        llStatsContainer.removeAllViews()
                        displayItems()

                        // Statuses
                        val tlStatusContainer = findViewById<LinearLayout>(R.id.tlStatusContainer)!!
                        tlStatusContainer.removeAllViews()
                        var row = TableRow(context)
                        for (status in Status.values()) {
                            val button = Button(context)
                            button.text = "${status.icon.repeat(controller.player.statusDict[status]!!)} ${status.name}"
                            button.textSize = 5f
                            button.layoutParams = TableRow.LayoutParams(-2,-2, 1f)
                            if (controller.player.statuses.contains(status)) {
                                button.setTextColor(Color.parseColor("#9999ff"))
                            }
                            button.setOnClickListener() {
                                controller.player.statusDict[status] = status.getNextManualPosition(controller.player.statusDict[status]!!)
                                setUpEverything()
                            }
                            row.addView(button)
                            Log.d("EEEEEEEEEEEEEEEEEEEEEEE", tlStatusContainer.childCount.toString())
                            if (row.children.count() == 3) {
                                tlStatusContainer.addView(row)
                                row = TableRow(context)
                            }
                        }
                        if (row.children.count() != 0) {
                            tlStatusContainer.addView(row)
                        }

                        // Curses spinner
                        val llDeckContainer = findViewById<LinearLayout>(R.id.llDeckContainer)!!
                        llDeckContainer.removeAllViews()
                        val npCurses = NumberPicker(context)
                        npCurses.minValue = 0
                        npCurses.maxValue = 10
                        npCurses.value = controller.deck.remainingCurses
                        npCurses.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                            controller.deck.remainingCurses = new
                        }
                        npCurses.setBackgroundColor(Color.parseColor("#470d02"))
                        llDeckContainer.addView(npCurses)

                        // HP spinner
                        val npHP = NumberPicker(context)
                        npHP.minValue = 0
                        npHP.maxValue = 26
                        npHP.value = controller.player.hp
                        npHP.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                            controller.player.hp = new
                        }
                        npHP.setBackgroundColor(Color.RED)
                        llStatsContainer.addView(npHP)

                        // Dings spinner
                        val npDings = NumberPicker(context)
                        npDings.minValue = 0
                        npDings.maxValue = 1000
                        npDings.value = controller.player.dings
                        npDings.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                            controller.player.dings = new
                        }
                        npDings.setBackgroundColor(Color.BLUE)
                        llStatsContainer.addView(npDings)
                        Log.d("EEEEEEEEEEEEEEEEEEEEEEE", tlStatusContainer.childCount.toString())
                    }

                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        requestWindowFeature(Window.FEATURE_NO_TITLE)
                        setContentView(R.layout.manage)

                        setUpEverything()

                        Log.d("HEy","Hey")
                    }
                }

                val dialog = CustomDialogClass(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                dialog.setOnDismissListener{
                    controller.log("Managed.")
                    controller.addUndoPoint()
                }
                dialog.show()

            }
        }
    }

    fun simplifyTheGamestate() {
        // Player
        controller.player.pierce = 0
        // Enemies
        for (i in controller.enemies.size-1 downTo 0) {
            if (controller.enemies[i].dead) {
                controller.enemies.removeAt(i)
            } else {
                val enemy = controller.enemies[i]
                enemy.extraTarget = false
                enemy.inMeleeRange = false
                enemy.inBallistaRange = false
                enemy.targeted = false
                enemy.muddled = false
                enemy.stunned = false
            }
        }
        //
        controller.log("Simplified the gamestate.")
    }

    fun buttonBehavior(button: Button, function: () -> Unit = {}) {
        startAction(button)

        try {
            function()
        } catch (e: Exception) {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage(e.message)
            dialogBuilder.setPositiveButton("Ignore") {_,_ ->}
            dialogBuilder.setNegativeButton("Crash the app lmao") {_,_ -> throw e}
            val alert = dialogBuilder.create()
            alert.setTitle("OW?")
            alert.show()
        }

        endAction(button)
    }
    fun startAction(button: Button) {
        controller.log("")
        controller.log("[${button.text.toString().uppercase()}]")
        effectQueue.add(Effect(wipe = true))
    }

    fun endAction(button: Button) {
        // Spinny button?
        if (controller.deck.activeCards.any{it.spinny} || controller.deck.discardPile.any{it.spinny}) {
            btnSpinny.isEnabled = true
            spinnyAnim.start()
        }
        // Discard button?
        if (controller.deck.activeCards.size > 0) {
            btnDiscard.isEnabled = true
            discardAnim.start()
        }
        // Undo+Redo buttons?
        btnUndo.isEnabled = controller.undosBack != controller.undoPoints.size - 1
        btnRedo.isEnabled = controller.undosBack != 0
        // Logs
        tvLog.text = controller.getShownLogs().joinToString(separator="\n")
    }
}