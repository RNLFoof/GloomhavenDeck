package com.example.gloomhavendeck

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout

enum class Status(val icon: String, val imageId: Int, val negative: Boolean=false, val roundBased: Boolean=true){
    DISARM("\uD83D\uDC4B", R.drawable.disarm, negative = true),
    IMMOBILE("\uD83E\uDD7E", R.drawable.immobile, negative = true),
    INVISIBLE("\uD83D\uDC7B", R.drawable.invisible),
    MUDDLE("❓", R.drawable.muddle, negative = true),
    POISON("☠", R.drawable.poison, negative = true, roundBased = false),
    REGENERATION("❤", R.drawable.regenerate, roundBased = false),
    STRENGTHEN("\uD83D\uDCAA", R.drawable.strengthen),
    STUN("💥", R.drawable.stun, negative = true),
    WOUND("\uD83E\uDE78", R.drawable.wound, negative = true, roundBased = false);

    fun getNextManualPosition(currentPosition: Int): Int {
        return if (roundBased) {
            (currentPosition + 2) % 3
        } else {
            1 - currentPosition
        }
    }

    fun getNextAutomaticPosition(currentPosition: Int): Int {
        return if (currentPosition == 0) {
            currentPosition
        }
        else if (roundBased) {
            currentPosition -1
        } else {
            currentPosition
        }
    }

    fun imageView(context: Context): ImageView {
        val imageView = ImageView(context)
        imageView.setImageResource(imageId)
        return imageView
    }

    override fun toString(): String {
        return icon
    }
}