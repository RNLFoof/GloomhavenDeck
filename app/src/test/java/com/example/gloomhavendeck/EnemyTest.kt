package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test
import org.junit.Before

internal class EnemyTest {

    @Test
    fun createMany() {
        Enemy.createMany("Verm Scout:1 2 3\nDog1 1 0", 7)
    }

    @Test
    fun oneOfEach() {
        val enemies = Enemy.oneOfEach().toMutableList()
        Assert.assertNotEquals(enemies.count(), 0)
        Assert.assertTrue(enemies.count() > 30)
        Assert.assertEquals(enemies.count() % 2, 0)
    }

    @Test
    fun oneOfEachInterestingGuy() {
        val enemies = Enemy.oneOfEachInterestingGuy().toMutableList()
        Assert.assertNotEquals(enemies.count(), 0)
        println(enemies)
    }

    @Test
    fun createOne() {
        Assert.assertThrows(Exception::class.java) { Enemy.createOne("", 7) }
        Assert.assertThrows(Exception::class.java) { Enemy.createOne("verm sc: 1\nverm sc: 2", 7) }

        val vermScout = Enemy.createOne("verm sc: 1", 7)
        Assert.assertEquals(vermScout.name, "VrmlScot1e")
        Assert.assertEquals(vermScout.maxHp, 15)
        Assert.assertFalse(vermScout.attackersGainDisadvantage)

        val ashblade = Enemy.createOne("aes ash: 1", 7)
        Assert.assertEquals(ashblade.name, "AsthAshb1e")
        Assert.assertEquals(ashblade.maxHp, 32)
        Assert.assertTrue(ashblade.attackersGainDisadvantage)
    }

    @Test
    fun teamsOfThisGuy() {
        val thisGuy = Enemy.createOne("verm sc: 1", 7)
        var finalN = -1
        val teamSizes = mutableSetOf<Int>()
        val dudeExponent = 4
        val dudeMultiplier = 4
        for ((n, team) in Enemy.teamsOfThisGuy(thisGuy, dudeExponent).withIndex()) {
            finalN = n
            teamSizes.add(team.count())
        }
        Assert.assertEquals(256, finalN + 1) // +1 because index vs size
        Assert.assertEquals(setOf(0*dudeMultiplier, 1*dudeMultiplier, 2*dudeMultiplier, 3*dudeMultiplier, 4*dudeMultiplier), teamSizes)
    }

    @Test
    fun interestingPipisTeams() {
        for ((n, team) in Enemy.interestingPipisTeams().withIndex()) {

            println(team)

            if (n > 1000) {
                break
            }
        }
    }

    @Test
    fun getAttacked() {
        val player = Player(Controller(), 44)
        val enemy = Enemy.createOne("Dog 2 0", 7)
        Assert.assertEquals(0, enemy.taken)
        enemy.getAttacked(Card(value=0), player)
        Assert.assertEquals(0, enemy.taken)
        enemy.getAttacked(Card(value=2), player)
        Assert.assertEquals(2, enemy.taken)
    }

    @Test
    fun deepCopy() {
        val enemy = Enemy.createOne("verm sc: 1", 7)
        Assert.assertEquals(
            enemy,
            enemy.deepCopy()
        )
        Assert.assertNotEquals(
            System.identityHashCode(enemy),
            System.identityHashCode(enemy.deepCopy())
        )
    }

    @Test
    fun equals() {
        Assert.assertEquals(Enemy("Dog 1 2"), Enemy("Dog 1 2"))
        Assert.assertNotEquals(Enemy("Dog 1 2"), Enemy("Dog 1 3"))
        Assert.assertNotEquals(Enemy("Dog 1 2"), null)
    }

    @Before
    fun setUp() {
    }
}