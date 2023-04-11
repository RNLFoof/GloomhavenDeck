package com.example.gloomhavendeck.meta

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.Controllable
import com.example.gloomhavendeck.Controller
import com.example.gloomhavendeck.SavableController
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@RequiresApi(Build.VERSION_CODES.O)
class UndoPoint() : Controllable() {
    private var heldStateJson = Json.encodeToString(Controller as SavableController)

    fun use() {
        Controller.let {
            val heldState: SavableController = Json.decodeFromString(heldStateJson)
            if (Controller.logger != null && heldState.logger != null) {
                Controller.logger!!.logsToHide =
                    Controller.logger!!.logsToHide + heldState.logger!!.logsToHide
            }
            Crap.fieldsFromInto(heldState, Controller)
        }
    }
}