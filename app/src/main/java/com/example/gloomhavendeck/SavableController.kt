package com.example.gloomhavendeck

import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import kotlinx.serialization.Serializable

@Serializable
open class SavableController {
    var saver: Saver? = null
    var logger: Logger? = null
    var player: Player? = null
    var inventory: Inventory? = null
    var deck: Deck? = null
    var pipis: Pipis? = null
}