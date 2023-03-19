package com.example.gloomhavendeck


import com.example.gloomhavendeck.meta.Saver
import org.junit.Assert
import org.junit.Test
import org.junit.Before

internal class SaverTest {
    lateinit var controller: Controller
    lateinit var saver: Saver

    @Test
    fun saveJsonToAndLoadJsonFrom() {
        saver.saveJsonTo(controller, "test.json")
        saver.loadJsonFrom<Controller>("test.json")
    }

    @Before
    fun setUp() {
        controller = Controller.newFullyStocked()
        saver = Saver(controller, "")
    }
}