package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
class Player(var maxHp: Int): Controllable() {
    init {
        Controller.player = this
    }

    var hp = maxHp
    var dings = 0

    var statusDict = HashMap<Status, Int>()
    init {
        for (status in Status.values()) {
            statusDict.putIfAbsent(status, 0)  // putIfAbsent, and not just setting, because this happens after an undo point is loaded, so otherwise the values are overwritten
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
            statusDict[Status.WOUND] = 0
        }
        if (statuses.contains(Status.POISON)) {
            statusDict[Status.POISON] = 0
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
}
