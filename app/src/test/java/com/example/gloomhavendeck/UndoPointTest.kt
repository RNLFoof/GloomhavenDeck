package com.example.gloomhavendeck


import com.example.gloomhavendeck.meta.UndoPoint
import org.junit.Assert
import org.junit.Test

internal class UndoPointTest {

    @Test
    fun usage() {
        val player = Player(26)
        Assert.assertEquals(player, Controller.player)
        val undoPoint = UndoPoint()
        player.hp = 1
        undoPoint.use()
        Assert.assertEquals(26, Controller.player!!.hp)
    }
}