package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test
import org.junit.Before

internal class PlayerTest {
    lateinit var controller: Controller
    lateinit var player: Player

    @Test
    fun heal() {
        for (amount in 1..7) {
            for (hp in 1 until player.maxHp) {
                for (poisoned in listOf(true, false)) {
                    for (wounded in listOf(true, false)) {
                        player.hp = hp
                        player.statusDict[Status.POISON] = if (poisoned) 2 else 0
                        player.statusDict[Status.WOUND] = if (wounded) 2 else 0

                        player.heal(amount)

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

    @Before
    fun setUp() {
        controller = Controller()
        player = Player(controller, 26)
    }
}