package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import com.gazman.signals.Signals
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

fun interface PlayerEvents{
    fun onStatusUpdate(old:HashMap<Status, Int>, new:HashMap<Status, Int>)
}

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
class Player(var maxHp: Int): Controllable() {
    init {
        Controller.player = this
    }

    var hp = maxHp
    var dings = 0

    @Contextual
    val playerSignal = Signals.signal(PlayerEvents::class)

    private var statusDict = HashMap<Status, Int>()
    init {
        for (status in Status.values()) {
            // checking, and not just setting, because this happens after an undo point is loaded, so otherwise the values are overwritten
            if (!statuses.contains(status)) {
                updateStatus(status, 0)
            }
        }
    }
    val statuses: Set<Status>
        get() {
            return statusDict.filterKeys { statusDict[it]!! > 0 }.keys
        }

    var powerPotionThreshold = 6
    var hpDangerThreshold = 10
    var pierce = 0
    var scenarioLevel = 7

    private var _discardedPipis = false
    var discardedPipis: Boolean
        get() = _discardedPipis
        set(value) {
            _discardedPipis = value
            val suggestedDiscardedCards = Integer.max(discardedCards, (if (discardedPipis) 1 else 0)+(if (discardedBallista) 1 else 0))
            if (discardedCards != suggestedDiscardedCards) {
                discardedCards = suggestedDiscardedCards
            }
        }

    private var _discardedBallista = false
    var discardedBallista: Boolean
        get() = _discardedBallista
        set(value) {
            _discardedBallista = value
            val suggestedDiscardedCards = Integer.max(discardedCards, (if (discardedPipis) 1 else 0)+(if (discardedBallista) 1 else 0))
            if (discardedCards != suggestedDiscardedCards) {
                discardedCards = suggestedDiscardedCards
            }
        }

    private var _discardedCards = 0
    var discardedCards: Int
        get() = _discardedCards
        set(value) {
            _discardedCards = Integer.max(0, value)
            if (value <= 0) {
                discardedPipis = false
                discardedBallista = false
            }
        }

    fun heal(amount: Int, viaItem: Boolean) {
        if (statuses.contains(Status.WOUND)) {
            updateStatus(Status.WOUND, 0)
        }
        if (statuses.contains(Status.POISON)) {
            updateStatus(Status.POISON, 0)
        } else {
            if (hp >= maxHp) {
                if (viaItem) {
                    throw ItemUnusableException("Already at max HP!")
                } else {
                    throw kotlin.Exception("Already at max HP!")
                }
            }
            hp = Integer.min(hp + amount, maxHp)
        }
    }

    fun checkStatus(status: Status): Int {
        return statusDict[status]!!
    }

    fun updateStatus(status: Status, value: Int) {
        val old: HashMap<Status, Int> = HashMap(statusDict)
        statusDict[status] = value
        val new: HashMap<Status, Int> = HashMap(statusDict)
        playerSignal.dispatcher.onStatusUpdate(old, new)
    }

    fun cycleStatus(status: Status) {
        updateStatus(status,
            status.getNextManualPosition(
                checkStatus(status)
            )
        )
    }
}
