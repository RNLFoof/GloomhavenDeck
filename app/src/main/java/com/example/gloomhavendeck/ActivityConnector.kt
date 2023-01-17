package com.example.gloomhavendeck

import android.app.Activity
import android.os.Build
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.*
import kotlinx.serialization.Transient

@RequiresApi(Build.VERSION_CODES.O)
class ActivityConnector(
    @Transient override var controller: Controller = Controller(),
    var activity: Activity,
    var llTopCardRow: LinearLayout,
    var llBottomCardRow: LinearLayout,
    var llItemRow: LinearLayout,
    var tvLog: TextView,
): Controllable() {
    var selectedCardRow: LinearLayout = llTopCardRow
    init {
        controller.activityConnector = this
    }

    @Transient
    var effectQueue = LinkedList<Effect>()
    val baseEffectSpeed = 1_000/3L
    var effectSpeed = baseEffectSpeed

    init {
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
        effectLoop.start()
    }

}