package com.example.gloomhavendeck


import com.example.gloomhavendeck.meta.Logger
import com.example.gloomhavendeck.meta.Saver
import com.example.gloomhavendeck.meta.UndoManager
import com.example.gloomhavendeck.meta.UndoPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import org.junit.Before

internal class UndoManagerTest {
    lateinit var controller: Controller
    lateinit var undoManager: UndoManager

    @Test
    fun encodeEach() {
        Json.encodeToString(Card())
        Json.encodeToString(Deck())
        Json.encodeToString(Enemy("dog 16 16"))
        Json.encodeToString(Inventory())
        Json.encodeToString(Logger())
        Json.encodeToString(Player(maxHp = 26))
        Json.encodeToString(Saver(filesDir = ""))
        Json.encodeToString(UndoManager())
        Json.encodeToString(UndoPoint())
        Json.encodeToString(UndoPoint())
    }

    @Before
    fun setUp() {
        controller = Controller()
        undoManager = UndoManager(controller)
    }
}