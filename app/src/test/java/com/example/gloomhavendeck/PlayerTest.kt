package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test
import org.junit.Before
import com.example.gloomhavendeck.PlayerEvents

internal class PlayerTest {
    lateinit var player: Player

    @Before
    fun setUp() {
        controller = Controller()
        player = Player(controller, 26)
    }

    @Test
    fun heal() {
        for (amount in 1..7) {
            for (hp in 1 until player.maxHp) {
                for (poisoned in listOf(true, false)) {
                    for (wounded in listOf(true, false)) {
                        player.hp = hp
                        player.updateStatus(Status.POISON, if (poisoned) 2 else 0)
                        player.updateStatus(Status.WOUND, if (wounded) 2 else 0)

                        player.heal(amount, false)

                        Assert.assertFalse(player.statuses.contains(Status.WOUND))
                        Assert.assertFalse(player.statuses.contains(Status.POISON))
                        Assert.assertTrue(player.hp <= player.maxHp)
                        if (poisoned) {
                            Assert.assertEquals(player.hp, hp)
                        } else {
                            Assert.assertTrue(player.hp == player.maxHp || player.hp == hp+amount)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun playerSignalTest() {
        var gotIt = false
        player.playerSignal.addListener{old, new ->
            gotIt = true
            Assert.assertEquals(0, old[Status.REGENERATION])
            Assert.assertEquals(69, new[Status.REGENERATION])
        }
        player.updateStatus(Status.REGENERATION, 69)
        assert(gotIt)
    }

    @Test
    fun checkStatusAndUpdateStatus() {
        player.updateStatus(Status.WOUND, 1)
        Assert.assertEquals(1, player.checkStatus(Status.WOUND))
        player.updateStatus(Status.POISON, 2)
        Assert.assertEquals(2, player.checkStatus(Status.POISON))
        player.updateStatus(Status.REGENERATION, 3)
        Assert.assertEquals(3, player.checkStatus(Status.REGENERATION))
    }

    @Test
    fun cycleStatus() {
        val roundBasedStatus = Status.values().first {it.roundBased}
        player.updateStatus(roundBasedStatus, 0)
        Assert.assertEquals(0, player.checkStatus(roundBasedStatus))
        player.cycleStatus(roundBasedStatus)
        Assert.assertEquals(2, player.checkStatus(roundBasedStatus))
        player.cycleStatus(roundBasedStatus)
        Assert.assertEquals(1, player.checkStatus(roundBasedStatus))
        player.cycleStatus(roundBasedStatus)
        Assert.assertEquals(0, player.checkStatus(roundBasedStatus))

        val notRoundBasedStatus = Status.values().first {!it.roundBased}
        player.updateStatus(notRoundBasedStatus, 0)
        Assert.assertEquals(0, player.checkStatus(notRoundBasedStatus))
        player.cycleStatus(notRoundBasedStatus)
        Assert.assertEquals(1, player.checkStatus(notRoundBasedStatus))
        player.cycleStatus(notRoundBasedStatus)
        Assert.assertEquals(0, player.checkStatus(notRoundBasedStatus))
        player.cycleStatus(notRoundBasedStatus)
        Assert.assertEquals(1, player.checkStatus(notRoundBasedStatus))
    }
}