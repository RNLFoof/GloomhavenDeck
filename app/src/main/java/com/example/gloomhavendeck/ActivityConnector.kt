package com.example.gloomhavendeck

import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.meta.Crap
import java.util.*
import kotlinx.serialization.Transient

@RequiresApi(Build.VERSION_CODES.O)
class ActivityConnector(
    
    var activity: Activity,
    var llTopCardRow: LinearLayout,
    var llBottomCardRow: LinearLayout,
    var llItemRow: LinearLayout,
    var tvLog: TextView,
): Controllable() {
    var selectedCardRow: LinearLayout = llTopCardRow
    init {
        Controller.activityConnector = this
    }

    // TODO Maybe an effect manager?
    @Transient
    var effectQueue = LinkedList<Effect>()
    val numberOfMediaPlayers = 5
    var mediaPlayerTarget = 0
    @Transient
    var mediaPlayersForSounds = arrayListOf<MediaPlayer>()

    val baseEffectSpeed = 1_000/3L
    var effectSpeed = baseEffectSpeed

    init {
        for (x in 1..numberOfMediaPlayers) {
            mediaPlayersForSounds.add(MediaPlayer())
        }

        val effectLoop = Thread {
            while (true) {
                Crap.crashProtector(activity) {
                    if (effectQueue.size > 0) {
                        val effect = effectQueue.removeFirst()
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
        }
        effectLoop.start()
    }

    fun nextMediaPlayer(): MediaPlayer {
        mediaPlayerTarget += 1
        mediaPlayerTarget %= numberOfMediaPlayers
        return mediaPlayersForSounds[mediaPlayerTarget]
    }
}