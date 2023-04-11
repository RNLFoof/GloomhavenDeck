package com.example.gloomhavendeck.meta

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.Controllable
import com.example.gloomhavendeck.Controller
import com.example.gloomhavendeck.SavableController
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@RequiresApi(Build.VERSION_CODES.O)
class UndoManager(): Controllable() {

    init {
        Controller.undoManager = this
    }

    @Transient // Honestly unsure why it's transient
    val undoPoints = mutableListOf<UndoPoint>()
    var undosBack = 0

    fun Undo() {
        undosBack += 1
        Log.d("undos", "Loading state ${undoPoints.size-undosBack-1+1}/${undoPoints.size}")
        undoPoints[undoPoints.size-undosBack-1].use()
    }

    fun Redo() {
        undosBack -= 1
        Log.d("undos", "Loading state ${undoPoints.size-undosBack+1}/${undoPoints.size}")
        undoPoints[undoPoints.size-undosBack-1].use()
    }

    fun addUndoPoint() {
        Controller.logger?.log("State saved.")
        // Override any "future" undos
        while (undosBack > 0) {
            undosBack -= 1
            undoPoints.removeLast()
        }
        // Add a new one
        undoPoints.add(UndoPoint())
        // Save
        Controller.saver?.let { it.saveJsonTo(Controller as SavableController, it.currentStateSavedAt) }
    }
}