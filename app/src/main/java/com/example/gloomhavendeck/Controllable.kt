package com.example.gloomhavendeck

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/*
    Object that can be synced with other objects via a controller.
 */
@Serializable
@RequiresApi(Build.VERSION_CODES.O)
open class Controllable constructor(
    @Transient/* If it could back to the controller, this would loop forever */
    open var controller: Controller = Controller(),

    val undoOnly: Iterable<String>? = null /* If present, only the fields listed are updated during an undo */
    ) {

}