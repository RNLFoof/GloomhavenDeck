package com.example.gloomhavendeck


import com.example.gloomhavendeck.meta.Crap
import com.example.gloomhavendeck.meta.Saver
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Before

internal class SaverTest {
    lateinit var saver: Saver

    @Before
    fun setUp() {
        Controller.fullyStock()
        saver = Saver("")
    }

    @Test
    fun saveJsonToAndLoadJsonFromOnClass() {
        var player = Player(20)
        saver.saveJsonTo(player, "test.json")
        player.hp = 10
        player = saver.loadJsonFrom("test.json")
        assertEquals(20, player.hp)
    }

    @Test
    fun saveJsonToAndLoadJsonFromOnController() {
        Controller.player!!.hp = 20
        saver.saveJsonTo(Controller as SavableController, "test.json")
        Controller.player!!.hp = 10
        val loadedController: SavableController = saver.loadJsonFrom("test.json")
        Crap.fieldsFromInto(loadedController, Controller)
        assertEquals(20, Controller.player!!.hp)
    }

    @Test
    fun updateControllerFrom() {
        Controller.player!!.hp = 20
        saver.saveJsonTo(Controller as SavableController, "test.json")
        Controller.player!!.hp = 10
        val loadedController: SavableController = saver.loadJsonFrom("test.json")
        assertEquals(20, loadedController.player!!.hp)
        saver.updateControllerFrom("test.json")
        assertEquals(20, Controller.player!!.hp)
    }
}