package com.example.gloomhavendeck.meta

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gloomhavendeck.Controllable
import com.example.gloomhavendeck.Controller
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@RequiresApi(Build.VERSION_CODES.O)
class UndoPoint(override var controller: Controller = Controller()) : Controllable(){
    private var heldStateJson = Json.encodeToString(controller)

    fun use() {
        val heldState: Controller = Json.decodeFromString(heldStateJson)
        if (controller.logger != null && heldState.logger != null) {
            controller.logger!!.logsToHide =
                controller.logger!!.logsToHide + heldState.logger!!.logsToHide
        }
        Crap.fieldsFromInto(heldState, controller)
    }
}