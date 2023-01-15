package com.example.gloomhavendeck

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import com.example.gloomhavendeck.meta.UndoManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
open class Controller(
) {
    var saver: Saver? = null
    var logger: Logger? = null
    var undoManager: UndoManager?= null
    var player = Player(26) // Like I guess I'll put 26 here?? fuck I should have structured this better
    var deck = Deck(this)
    var enemies: MutableList<Enemy> = mutableListOf()
}