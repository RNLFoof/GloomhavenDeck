package com.example.gloomhavendeck

import android.annotation.SuppressLint
import android.graphics.ColorFilter
import android.media.MediaPlayer
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.size
import kotlinx.serialization.Transient
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class Effect(var sound: SoundBundle? = null, var card: Int? = null, var wipe: Boolean = false,
             var selectTopRow: Boolean = false, var selectBottomRow: Boolean = false,
             var showBottomRow: Boolean = false, var hideBottomRow: Boolean = false,
             var showItemRow: Boolean = false, var hideItemRow: Boolean = false,
             var newItemRowDisplay: List<Boolean>? = null, var cardForeground: Int? = null): Controllable()
{
    var speed = Controller.activityConnector?.effectSpeed ?: 1
    @SuppressLint("UseCompatLoadingForDrawables")
    fun run() {
        if (Controller.activityConnector != null) {
            if (sound != null) {
                val mediaPlayer: MediaPlayer = MediaPlayer.create(Controller.activityConnector!!.activity, sound!!.getSound() as Int)
                mediaPlayer.start()
            }
            if (card != null) {
                val imageView = ImageView(Controller.activityConnector!!.activity)
                imageView.setImageResource(card!!)
                imageView.rotation = (Random().nextFloat()*1-0.5).toFloat() // This masks bad scanning lol
                imageView.adjustViewBounds = true

                if (cardForeground != null) {
                    imageView.foreground = Controller.activityConnector!!.activity.getDrawable(
                        cardForeground!!
                    )
                }

                // Show the bottom row if there's already three cards
                // If it's already visible then whatever
                if (Controller.activityConnector!!.llTopCardRow.size == 3) {
                    showBottomRow = true
                }
                Controller.activityConnector!!.activity.runOnUiThread {
                    Controller.activityConnector!!.selectedCardRow.addView(imageView)
                }
            }
            if (wipe) {
                Controller.activityConnector!!.activity.runOnUiThread {
                    Controller.activityConnector!!.llTopCardRow.removeAllViews()
                    Controller.activityConnector!!.llBottomCardRow.removeAllViews()
                }
            }
            if (selectTopRow) {
                Controller.activityConnector!!.selectedCardRow = Controller.activityConnector!!.llTopCardRow
            }
            if (selectBottomRow) {
                Controller.activityConnector!!.selectedCardRow = Controller.activityConnector!!.llBottomCardRow
            }

            if (showBottomRow) {
                Controller.activityConnector!!.activity.runOnUiThread {
                    Controller.activityConnector!!.llBottomCardRow.visibility = View.VISIBLE
                }
            }
            if (hideBottomRow) {
                Controller.activityConnector!!.activity.runOnUiThread {
                    Controller.activityConnector!!.llBottomCardRow.visibility = View.GONE
                }
            }

            if (showItemRow) {
                Controller.activityConnector!!.activity.runOnUiThread {
                    Controller.activityConnector!!.llItemRow.visibility = View.VISIBLE
                }
            }
            if (hideItemRow) {
                Controller.activityConnector!!.activity.runOnUiThread {
                    Controller.activityConnector!!.llItemRow.visibility = View.GONE
                }
            }
            // TODO move this logic to the inventory
            if (newItemRowDisplay != null) {
                Controller.activityConnector!!.activity.runOnUiThread {
                    Controller.activityConnector!!.llItemRow.removeAllViews()
                }
                if (Controller.inventory != null) {
                    for ((i, item) in (Controller.inventory!!.allItemsSorted().withIndex())) {
                        val imageView = item.getImageView(Controller.activityConnector!!.activity, newItemRowDisplay!![i], false)
                        Controller.activityConnector!!.activity.runOnUiThread {
                            Controller.activityConnector!!.llItemRow.addView(imageView)
                        }
                    }
                }
            }
        }
    }
}