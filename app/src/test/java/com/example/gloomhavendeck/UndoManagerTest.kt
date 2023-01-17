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
    lateinit var controller: Controller
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
        Controller.doWithoutDestroyingTheUniverse {
            for (o in allControllables()) {
                Json.encodeToString(o)
            }
            Json.encodeToString(Controller.newFullyStocked())
        }
    }

    @Test
    fun encodeDecodeController() {
        Controller.doWithoutDestroyingTheUniverse {
            val fullyStockedController = Controller.newFullyStocked()
            val encoded = Json.encodeToString(fullyStockedController)
            val decoded: Controller = Json.decodeFromString( encoded )
            Assert.assertEquals(encoded, Json.encodeToString(decoded))
        }
    }

    @Before
    fun setUp() {
        controller = Controller()
        undoManager = UndoManager(controller)
    }
}