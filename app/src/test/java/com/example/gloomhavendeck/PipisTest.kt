package com.example.gloomhavendeck


import com.example.gloomhavendeck.Enemy.Companion.createMany
import com.example.gloomhavendeck.Enemy.Companion.interestingPipisTeams
import com.example.gloomhavendeck.meta.Saver
import org.junit.Assert
import org.junit.Test
import org.junit.Before

internal class PipisTest {
    lateinit var controller: Controller
    lateinit var pipis: Pipis

    @Test
    fun generateNerflessCounterparts() {
        val duplicateEnemies = createMany("Dog1 9 0\nDog1 9 0", 7).toList()
        Assert.assertThrows(DuplicateEnemyNameException::class.java) {
            Pipis.generateNerflessCounterparts(duplicateEnemies, 7)
        }

        val uniqueEnemies = createMany("Dog1 9 0\nDog2 9 0", 7).toList()
        val counterparts = Pipis.generateNerflessCounterparts(uniqueEnemies, 7)
        for (e in uniqueEnemies) {
            Assert.assertEquals(
                e.toString(),
                counterparts[e.name].toString()
            )
            Assert.assertEquals(
                System.identityHashCode(e),
                System.identityHashCode(e)
            )
            Assert.assertNotEquals(
                System.identityHashCode(e),
                System.identityHashCode(counterparts[e.name])
            )
        }
    }

    @Before
    fun setUp() {
        controller = Controller.newFullyStocked()
        pipis = Pipis(controller)
    }
}