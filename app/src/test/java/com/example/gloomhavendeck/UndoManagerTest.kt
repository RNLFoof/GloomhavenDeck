package com.example.gloomhavendeck


import com.example.gloomhavendeck.meta.Crap.Companion.allControllables
import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import com.example.gloomhavendeck.meta.UndoManager
import com.example.gloomhavendeck.meta.UndoPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import org.junit.Before

internal class UndoManagerTest {
    lateinit var undoManager: UndoManager

//    fun allExpectedToBeSaved(): Array<*> {
//        // I don't know how to not do this by hand. aaaaaaaa
//        val otherStuff = allControllables() as Array<*>
//        return arrayOf(
//            Card(),
//            Enemy("dog 16 16"),
//            Status.POISON,
//            Item.LUCKY_EYE
//        ) + otherStuff
//    }

    @Test
    fun encodeEach() {
        Controller.let {
            for (o in allControllables()) {
                Json.encodeToString(o)
            }
            Json.encodeToString(Controller.fullyStock())
        }
    }

    @Before
    fun setUp() {
        Controller.fullyStock()
        undoManager = UndoManager()
    }
}