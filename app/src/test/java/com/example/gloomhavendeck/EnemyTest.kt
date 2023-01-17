package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test
import org.junit.Before

internal class EnemyTest {

    @Test
    fun createMany() {
        Enemy.createMany("Verm Scout:1 2 3", 7)
    }

    @Test
    fun oneOfEach() {
        val enemies = Enemy.oneOfEach().toMutableList()
        Assert.assertNotEquals(enemies.count(), 0)
        Assert.assertTrue(enemies.count() > 30)
        Assert.assertEquals(enemies.count() % 2, 0)
    }

    @Before
    fun setUp() {
    }
}