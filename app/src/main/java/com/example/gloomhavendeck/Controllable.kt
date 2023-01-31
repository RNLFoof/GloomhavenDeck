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
open class Controllable(
    @Transient/* If it could back to the controller, this would loop forever */
    open var controller: Controller = Controller()
)