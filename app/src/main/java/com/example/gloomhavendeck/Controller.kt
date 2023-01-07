package com.example.gloomhavendeck

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
open class Controller(private val filesDir: String) {
    var player = Player(26) // Like I guess I'll put 26 here?? fuck I should have structured this better
    var deck = Deck(this)
    var enemies: MutableList<Enemy> = mutableListOf()

    // Undoing
    @Transient
    val undoPoints = mutableListOf<UndoPoint>()
    var undosBack = 0

    // Logging
    var logList = mutableListOf<String>()
    @Transient var logIndent = 0 // How many spaces to insert before a log, used to indicate that one action is part of another
    @Transient var logMuted = false // So you can make it shut up
    var logCount = 0 // How many logs have been made in general, used instead of index so old stuff can be removed
    var logsToHide = 0 // Used to go back and forth while undoing without like, making entire separate copies of the logs
    private val currentStateSavedAt = Paths.get(filesDir, "current_state.json").toString()

    fun log(text: String) {
        Log.d("heyyyy", text)
        if (!logMuted) {
            // Override any "future" logs
            while (logsToHide > 0 && logList.size > 0) {
                logsToHide -= 1
                logList.removeLast()
            }
            logsToHide = 0 // So that if there's more to hide than there is it still resets
            logList.add("----".repeat(logIndent) + text)
            while (logList.size > 100) {
                logList.removeFirst()
            }
            logCount += 1
        }
    }

    fun getShownLogs(): MutableList<String> {
        Log.d("undos", "Hiding $logsToHide logs. Final log is ${logList.last()}")
        return logList.subList(0, Integer.max(0, logList.size - logsToHide))
    }

    fun addUndoPoint() {
        log("State saved.")
        // Override any "future" undos
        while (undosBack > 0) {
            undosBack -= 1
            undoPoints.removeLast()
        }
        // Add a new one
        undoPoints.add(getUndoPoint())
        // Save
        val currentStateJson = Json.encodeToString(this)
        File(currentStateSavedAt).writeText(currentStateJson)
    }

    // Done like this so the object can be replaced with an expanded one.
    open fun getUndoPoint(): UndoPoint {
        return UndoPoint(this)
    }

    fun Undo() {
        undosBack += 1
        Log.d("undos", "Loading state ${undoPoints.size-undosBack-1+1}/${undoPoints.size}")
        undoPoints[undoPoints.size-undosBack-1].use(this)
    }

    fun Redo() {
        undosBack -= 1
        Log.d("undos", "Loading state ${undoPoints.size-undosBack+1}/${undoPoints.size}")
        undoPoints[undoPoints.size-undosBack-1].use(this)
    }

    fun InsertSelfIntoAllChildren() {
        deck.controller = this
    }
}