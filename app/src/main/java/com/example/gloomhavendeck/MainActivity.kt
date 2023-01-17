package com.example.gloomhavendeck
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
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
import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import com.example.gloomhavendeck.meta.UndoManager
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    lateinit var btnSpinny : Button
    lateinit var btnDiscard : Button
    lateinit var btnUndo : Button
    lateinit var btnRedo : Button

    lateinit var controller : Controller
    var enemyOrder: MutableList<String> = mutableListOf()

    lateinit var discardAnim: ObjectAnimator
    lateinit var spinnyAnim: ObjectAnimator

//    inner class MainActivityPlayer(maxHp: Int) : Player(maxHp) {
//        init {
//            inventory = MainActivityInventory()
//        }
//        override fun useItem(item: Item, deck: Deck, viaPipis: Boolean) {
//            effectQueue.add(Effect(card = item.graphic, sound = item.sound, selectTopRow = true))
//            controller.logger?.log("Using a $item...")
//            controller.logIndent += 1
//            super.useItem(item, controller.deck!!, viaPipis)
//            controller.logIndent -= 1
//        }
//        override fun deactivateItem(item: Item, deck: Deck, viaPipis: Boolean) {
//            effectQueue.add(Effect(sound = item.deactivationSound))
//            controller.logger?.log("Deactivating $item...")
//            controller.logIndent += 1
//            super.deactivateItem(item, controller.deck!!, viaPipis)
//            controller.logIndent -= 1
//        }
//    }


    @RequiresApi(Build.VERSION_CODES.Q)
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

        controller = Controller()
        Saver(controller, applicationContext.filesDir.canonicalPath)
        Inventory(controller)
        Deck(controller)
        Logger(controller)
        UndoManager(controller)

        ActivityConnector(controller,
            activity = this,
            llBottomCardRow = findViewById(R.id.llBottomCardRow),
            llTopCardRow = findViewById(R.id.llTopCardRow),
            llItemRow = findViewById(R.id.llItemRow),
            tvLog = findViewById(R.id.tvLog)
        )

        runOnUiThread {
            val loadBuilder = AlertDialog.Builder(this@MainActivity)

            loadBuilder.setPositiveButton("Yeah") { dialog, which ->
                controller.saver!!.updateControllerFrom(controller.saver!!.currentStateSavedAt)
            }

            loadBuilder.setNegativeButton("No") { _, _->
                val deckBuilder = AlertDialog.Builder(this@MainActivity)
                deckBuilder.setPositiveButton("Three Spears") { _, _ ->
                    Player(controller, 26)
                    controller.deck!!.addBaseDeckThreeSpears()
                    controller.inventory?.initializeThreeSpears()
                }
                deckBuilder.setNegativeButton("Eye") { _, _ ->
                    Player(controller,14)
                    controller.deck!!.addBaseDeckEye()
                    controller.inventory?.initializeEye()
                }
                deckBuilder.setTitle("Class?")
                deckBuilder.show()

                controller.enemies = Enemy.createMany("""Dog1 12
2 8
3 15,shield 1
4 4
vermling scout 7: 1 2 3 n5 6""", controller.player?.scenarioLevel ?: 7).toMutableList()
            }
            loadBuilder.setTitle("Load?")
            loadBuilder.show()
        }

        btnDiscard = findViewById(R.id.btnDiscard)
        btnRedo = findViewById(R.id.btnRedo)
        btnSpinny = findViewById(R.id.btnSpinny)
        btnUndo = findViewById(R.id.btnUndo)

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

        // Adding cards
        btnBless.setOnClickListener {
            buttonBehavior(btnBless) {
                controller.deck!!.bless(true)
            }
        }

        btnCurse.setOnClickListener {
            buttonBehavior(btnCurse) {
                controller.deck!!.curse(true)
            }
        }

        // Viewing cards
        btnCheat.setOnClickListener {
            buttonBehavior(btnCheat) {
                val dialogBuilder = AlertDialog.Builder(this)

                dialogBuilder.setMessage("Are you sure you want to view all card states?")
                    .setPositiveButton("Yeah lol") { _, _ ->
                        run {
                            val innerDialogBuilder = AlertDialog.Builder(this)
                            if (controller.deck != null) {
                                innerDialogBuilder.setMessage("Draw pile:\n${controller.deck!!.drawPile}\n\nActive cards:\n${controller.deck!!.activeCards}\n\nDiscard pile:\n${controller.deck!!.discardPile}")
                            }
                            val alert = innerDialogBuilder.create()
                            alert.setTitle("Cheat!")
                            alert.show()
                        }
                    }

                val alert = dialogBuilder.create()
                alert.setTitle("Cheat?")
                alert.show()
            }
        }

        btnViewCards.setOnClickListener {
            buttonBehavior(btnViewCards) {
                val dialogBuilder = AlertDialog.Builder(this)
                if (controller.deck != null) {
                    dialogBuilder.setMessage("Active cards:\n${controller.deck!!.activeCards}\n\nDiscard pile:\n${controller.deck!!.discardPile}")
                }
                val alert = dialogBuilder.create()
                alert.setTitle("Cards")
                alert.show()
            }
        }

        // Undos
        btnUndo.setOnClickListener {
            buttonBehavior(btnUndo) {
                controller.undoManager?.Undo()
                endAction(btnUndo)
            }
        }

        btnRedo.setOnClickListener {
            buttonBehavior(btnRedo) {
                controller.undoManager?.Redo()
                endAction(btnRedo)
            }
        }

        // Attacks
        btnAttack.setOnClickListener {
            buttonBehavior(btnAttack) {
                controller.activityConnector?.effectQueue?.add(Effect(controller, hideItemRow=true))
                controller.deck!!.attack(userDirectlyRequested = true)
            }
        }

        btnAdvantage.setOnClickListener {
            buttonBehavior(btnAdvantage) {
                controller.activityConnector?.effectQueue?.add(Effect(controller, hideItemRow=true))
                controller.deck!!.advantage(userDirectlyRequested = true)
            }
        }

        btnDisadvantage.setOnClickListener {
            buttonBehavior(btnDisadvantage) {
                controller.activityConnector?.effectQueue?.add(Effect(controller, hideItemRow=true))
                controller.deck!!.disadvantage(userDirectlyRequested = true)
            }
        }

        btnPipis.setOnClickListener {
            buttonBehavior(btnPipis) {
                val dialogBuilder = AlertDialog.Builder(this)

                var i = 0
                dialogBuilder.setItems(arrayOf(
                    "Enemies",
                    "Enemy Order",
                    "Enemy Menu",
                    "Power Potion Threshold (Currently ${controller.player!!.powerPotionThreshold})",
                    "HP Danger Threshold (Currently ${controller.player!!.hpDangerThreshold})",
                    "Pierce (Currently ${controller.player!!.pierce})",
                    "Scenario Level (Currently ${controller.player!!.scenarioLevel})",
                    "Discard Status (Currently ${controller.player!!.discardedCards})",
                    "Go")
                ) { _, which ->
                    when (which) {
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
                                        controller.enemies = Enemy.createMany(text, controller.player!!.scenarioLevel).toMutableList()
                                        text = controller.enemies.joinToString(separator = "\n") { it.toString() }
                                        title = "Result:"
                                    } catch (e: Exception) {
                                        title = e.message.toString()
                                    }
                                    showAlert()
                                    controller.sortEnemies(enemyOrder)
                                    controller.logger?.log("Updated controller.enemies.")
                                    controller.undoManager?.addUndoPoint()
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
                                        crashProtector {
                                            enemyNames.remove(enemyName)
                                            enemyOrder.add(enemyName)
                                            if (enemyNames.size > 0) {
                                                showAlert()
                                            } else {
                                                controller.logger?.log("Updated enemy order.")
                                                controller.sortEnemies(enemyOrder)
                                                controller.undoManager?.addUndoPoint()
                                                endAction(btnPipis)
                                            }
                                            alert.cancel()
                                        }
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
                                    crashProtector{ changeTakenTo(new) }
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
                                    crashProtector{ changeTakenTo(enemy.taken+1) }
                                }
                                llRowRow.addView(btnPlus1)
                                // +5
                                btnPlus5.text = "+5"
                                btnPlus5.layoutParams = ViewGroup.LayoutParams(200,100)
                                btnPlus5.setOnClickListener() {
                                    crashProtector{ changeTakenTo(enemy.taken+5) }
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
                                    crashProtector{ enemy.targeted = !enemy.targeted }
                                }
                                llRow.addView(cbTargeted)
                                // Ballista checkbox
                                val cbBallista = CheckBox(this)
                                cbBallista.textSize = textSize
                                cbBallista.text = "Blst"
                                cbBallista.isChecked = enemy.inBallistaRange
                                cbBallista.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                    crashProtector{ enemy.inBallistaRange = !enemy.inBallistaRange }
                                }
                                llRow.addView(cbBallista)
                                // Extra Target checkbox
                                val cbExtraTarget = CheckBox(this)
                                cbExtraTarget.textSize = textSize
                                cbExtraTarget.text = "ExTr"
                                cbExtraTarget.isChecked = enemy.extraTarget
                                cbExtraTarget.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                    crashProtector{ enemy.extraTarget = !enemy.extraTarget }
                                }
                                llRow.addView(cbExtraTarget)
                                // Melee Range checkbox
                                val cbMeleeRange = CheckBox(this)
                                cbMeleeRange.textSize = textSize
                                cbMeleeRange.text = "MlRn"
                                cbMeleeRange.isChecked = enemy.inMeleeRange
                                cbMeleeRange.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                    crashProtector{ enemy.inMeleeRange = !enemy.inMeleeRange }
                                }
                                llRow.addView(cbMeleeRange)
                                // Retaliate Range checkbox
                                val cbRetaliateRange = CheckBox(this)
                                cbRetaliateRange.textSize = textSize
                                cbRetaliateRange.text = "RtRn"
                                cbRetaliateRange.isChecked = enemy.inRetaliateRange
                                cbRetaliateRange.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                    crashProtector{ enemy.inRetaliateRange = !enemy.inRetaliateRange }
                                }
                                llRow.addView(cbRetaliateRange)
                                // Poisoned checkbox
                                val cbPoisoned = CheckBox(this)
                                cbPoisoned.textSize = textSize
                                cbPoisoned.text = "Poisn"
                                cbPoisoned.isChecked = enemy.poisoned
                                cbPoisoned.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                    crashProtector{ enemy.poisoned = !enemy.poisoned }
                                }
                                llRow.addView(cbPoisoned)
                            }
                            alert.setView(scrollView)
                            alert.setOnDismissListener{
                                crashProtector {
                                    controller.logger?.log("Updated controller.enemies via menu.")
                                    controller.undoManager?.addUndoPoint()
                                    endAction(btnPipis)
                                }
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
                            input.setText(controller.player!!.powerPotionThreshold.toString())
                            alert.setView(input)
                            alert.setPositiveButton("Set") { _, _ ->
                                controller.player!!.powerPotionThreshold = input.text.toString().toInt()
                                controller.logger?.log("Updated power pot threshold.")
                                controller.undoManager?.addUndoPoint()
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
                            input.setText(controller.player!!.hpDangerThreshold.toString())
                            alert.setView(input)
                            alert.setPositiveButton("Set") { _, _ ->
                                controller.player!!.hpDangerThreshold = input.text.toString().toInt()
                                controller.logger?.log("Updated HP danger threshold.")
                                controller.undoManager?.addUndoPoint()
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
                            input.setText(controller.player!!.pierce.toString())
                            alert.setView(input)
                            alert.setPositiveButton("Set") { _, _ ->
                                controller.player!!.pierce = input.text.toString().toInt()
                                controller.logger?.log("Updated pierce.")
                                controller.undoManager?.addUndoPoint()
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
                            input.setText(controller.player!!.scenarioLevel.toString())
                            alert.setView(input)
                            alert.setPositiveButton("Set") { _, _ ->
                                controller.player!!.scenarioLevel = input.text.toString().toInt()
                                controller.logger?.log("Updated scenario level.")
                                controller.undoManager?.addUndoPoint()
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
                            npDiscarded.value = controller.player!!.discardedCards
                            npDiscarded.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                                crashProtector {
                                    controller.player!!.discardedCards = new
                                    cbBallista.isChecked = controller.player!!.discardedBallista
                                    cbPipis.isChecked = controller.player!!.discardedPipis
                                }
                            }
                            linearLayout.addView(npDiscarded)
                            // Targeted checkbox
                            cbPipis.text = "Discarded Pipis"
                            cbPipis.isChecked = controller.player!!.discardedPipis
                            cbPipis.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                crashProtector {
                                    controller.player!!.discardedPipis = on
                                    npDiscarded.value = controller.player!!.discardedCards
                                }
                            }
                            linearLayout.addView(cbPipis)
                            // Ballista checkbox
                            cbBallista.text = "Discarded Ballista"
                            cbBallista.isChecked = controller.player!!.discardedBallista
                            cbBallista.setOnCheckedChangeListener { _: CompoundButton, on: Boolean ->
                                crashProtector {
                                    controller.player!!.discardedBallista = on
                                    npDiscarded.value = controller.player!!.discardedCards
                                }
                            }
                            linearLayout.addView(cbBallista)
                            alert.setOnDismissListener{
                                crashProtector {
                                    controller.logger?.log("Updated discard status.")
                                    controller.undoManager?.addUndoPoint()
                                    endAction(btnPipis)
                                }
                            }
                            alert.show()
                        }
                        // Go
                        i++ -> {
                            controller.activityConnector?.effectQueue?.add(Effect(controller, showItemRow=true))
                            controller.inventory?.displayChangedInventory()
                            controller.activityConnector?.effectSpeed = 1_000/4L
                            controller.deck!!.pipis(controller.player!!, controller.enemies)
                            controller.activityConnector?.effectSpeed = controller.activityConnector!!.baseEffectSpeed
                            simplifyTheGamestate()
                            endAction(btnPipis)
                        }
                    }
                }

                val alert = dialogBuilder.create()
                alert.setTitle("Pipis Menu")
                alert.show()
            }
        }

        // Card movement
        btnDiscard.setOnClickListener {
            buttonBehavior(btnDiscard) {
                controller.deck!!.activeCardsToDiscardPile(true)
                btnDiscard.isEnabled = false
                discardAnim.end()
            }
        }

        btnSpinny.setOnClickListener {
            buttonBehavior(btnSpinny) {
                controller.deck!!.activeCardsToDiscardPile(true)
                controller.deck!!.discardPileToDrawPile(true)
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
                controller.undoManager?.addUndoPoint()
            }
        }

        btnManage.setOnClickListener {
            buttonBehavior(btnManage) {
                class CustomDialogClass(var context: Activity, theme: Int) : Dialog(context, theme) {
                    fun displayItems() {
                        val llItemContainer = findViewById<LinearLayout>(R.id.llItemContainer)!!
                        llItemContainer.removeAllViews()
                        var row = LinearLayout(context)
                        for ((n, item) in (controller.inventory!!.usableItems + controller.inventory!!.unusableItems + controller.inventory!!.activeItems).sorted().withIndex()) {
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
                            val imageView = item.getImageView(context, controller.inventory!!.usableItems.contains(item), controller.inventory!!.activeItems.contains(item))

                            if (!item.permanent) {
                                // Click
                                imageView.setOnClickListener() {
                                    try {
                                        if (controller.inventory!!.usableItems.contains(item)) {
                                            controller.inventory!!.useItem(item, false)
                                        }
                                        else if (controller.inventory!!.activeItems.contains(item)) {
                                            controller.inventory!!.deactivateItem(item, false)
                                        } else {
                                            controller.inventory!!.regainItem(item)
                                        }
                                        setUpEverything()
                                    } catch (e: ItemUnusableException) {

                                    }
                                }
                                // Long Click
                                if (controller.inventory!!.usableItems.contains(item)) {
                                    imageView.setOnLongClickListener() {
                                        controller.inventory!!.loseItem(item)
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
                            button.text = "${status.icon.repeat(controller.player!!.statusDict[status]!!)} ${status.name}"
                            button.textSize = 5f
                            button.layoutParams = TableRow.LayoutParams(-2,-2, 1f)
                            if (controller.player!!.statuses.contains(status)) {
                                button.setTextColor(Color.parseColor("#9999ff"))
                            }
                            button.setOnClickListener() {
                                controller.player!!.statusDict[status] = status.getNextManualPosition(controller.player!!.statusDict[status]!!)
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
                        npCurses.value = controller.deck!!.remainingCurses
                        npCurses.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                            controller.deck!!.remainingCurses = new
                        }
                        npCurses.setBackgroundColor(Color.parseColor("#470d02"))
                        llDeckContainer.addView(npCurses)

                        // HP spinner
                        val npHP = NumberPicker(context)
                        npHP.minValue = 0
                        npHP.maxValue = controller.player!!.maxHp
                        npHP.value = controller.player!!.hp
                        npHP.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                            controller.player!!.hp = new
                        }
                        npHP.setBackgroundColor(Color.RED)
                        llStatsContainer.addView(npHP)

                        // Dings spinner
                        val npDings = NumberPicker(context)
                        npDings.minValue = 0
                        npDings.maxValue = 1000
                        npDings.value = controller.player!!.dings
                        npDings.setOnValueChangedListener { numberPicker: NumberPicker, old: Int, new: Int ->
                            controller.player!!.dings = new
                        }
                        npDings.setBackgroundColor(Color.BLUE)
                        llStatsContainer.addView(npDings)
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
                    controller.logger?.log("Managed.")
                    controller.undoManager?.addUndoPoint()
                }
                dialog.show()

            }
        }
    }

    fun simplifyTheGamestate() {
        // Player
        controller.player!!.pierce = 0
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
        controller.logger?.log("Simplified the gamestate.")
    }

    fun crashProtector(function: () -> Unit = {}) {
        Crap.crashProtector(this, function)
    }

    fun buttonBehavior(button: Button, function: () -> Unit = {}) {
        startAction(button)
        crashProtector(function)
        endAction(button)
    }
    fun startAction(button: Button) {
        controller.logger?.log("")
        controller.logger?.log("[${button.text.toString().uppercase()}]")
        controller.activityConnector?.effectQueue?.add(Effect(controller, wipe = true))
    }

    fun endAction(button: Button) {
        // Spinny button?
        if (controller.deck!!.activeCards.any{it.spinny} || controller.deck!!.discardPile.any{it.spinny}) {
            btnSpinny.isEnabled = true
            spinnyAnim.start()
        }
        // Discard button?
        if (controller.deck!!.activeCards.size > 0) {
            btnDiscard.isEnabled = true
            discardAnim.start()
        }
        // Undo+Redo buttons?
        btnUndo.isEnabled = controller.undoManager?.undosBack != controller.undoManager?.undoPoints?.size?.minus(
            1
        )
        btnRedo.isEnabled = controller.undoManager?.undosBack != 0
    }
}