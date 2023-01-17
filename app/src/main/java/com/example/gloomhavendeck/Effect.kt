package com.example.gloomhavendeck

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.size
import kotlinx.serialization.Transient
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class Effect(@Transient override var controller: Controller = Controller(),
             var sound: SoundBundle? = null, var card: Int? = null, var wipe: Boolean = false,
             var selectTopRow: Boolean = false, var selectBottomRow: Boolean = false,
             var showBottomRow: Boolean = false, var hideBottomRow: Boolean = false,
             var showItemRow: Boolean = false, var hideItemRow: Boolean = false,
             var newItemRowDisplay: List<Boolean>? = null): Controllable(controller)
{
    var speed = controller.activityConnector?.effectSpeed ?: 1
    @SuppressLint("UseCompatLoadingForDrawables")
    fun run() {
        if (controller.activityConnector != null) {
            if (sound != null) {
                val mediaPlayer: MediaPlayer = MediaPlayer.create(controller.activityConnector!!.activity, sound!!.getSound() as Int)
                mediaPlayer.start()
            }
            if (card != null) {
                val imageView = ImageView(controller.activityConnector!!.activity)
                imageView.setImageResource(card!!)
                imageView.rotation = (Random().nextFloat()*1-0.5).toFloat() // This masks bad scanning lol
                imageView.adjustViewBounds = true
                // Show the bottom row if there's already three cards
                // If it's already visible then whatever
                if (controller.activityConnector!!.llTopCardRow.size == 3) {
                    showBottomRow = true
                }
                controller.activityConnector!!.activity.runOnUiThread {
                    controller.activityConnector!!.selectedCardRow.addView(imageView)
                }
            }
            if (wipe) {
                controller.activityConnector!!.activity.runOnUiThread {
                    controller.activityConnector!!.llTopCardRow.removeAllViews()
                    controller.activityConnector!!.llBottomCardRow.removeAllViews()
                }
            }
            if (selectTopRow) {
                controller.activityConnector!!.selectedCardRow = controller.activityConnector!!.llTopCardRow
            }
            if (selectBottomRow) {
                controller.activityConnector!!.selectedCardRow = controller.activityConnector!!.llBottomCardRow
            }

            if (showBottomRow) {
                controller.activityConnector!!.activity.runOnUiThread {
                    controller.activityConnector!!.llBottomCardRow.visibility = View.VISIBLE
                }
            }
            if (hideBottomRow) {
                controller.activityConnector!!.activity.runOnUiThread {
                    controller.activityConnector!!.llBottomCardRow.visibility = View.GONE
                }
            }

            if (showItemRow) {
                controller.activityConnector!!.activity.runOnUiThread {
                    controller.activityConnector!!.llItemRow.visibility = View.VISIBLE
                }
            }
            if (hideItemRow) {
                controller.activityConnector!!.activity.runOnUiThread {
                    controller.activityConnector!!.llItemRow.visibility = View.GONE
                }
            }
            if (newItemRowDisplay != null) {
                controller.activityConnector!!.activity.runOnUiThread {
                    controller.activityConnector!!.llItemRow.removeAllViews()
                }
                if (controller.inventory != null) {
                    for ((i, item) in (controller.inventory!!.allItemsSorted().withIndex())) {
                        val imageView = item.getImageView(controller.activityConnector!!.activity, newItemRowDisplay!![i], false)
                        controller.activityConnector!!.activity.runOnUiThread {
                            controller.activityConnector!!.llItemRow.addView(imageView)
                        }
                    }
                }
            }
        }
    }
}