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
                e,
                counterparts[e.name]
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

    @Test
    fun attackEnemy() {
        // TODO Split attackEnemy from being a submethod, and make this test more direct
        val enemy = Enemy.createOne("Dog1 6 4", 1)
        val counterpart = enemy.deepCopy()

        fun attackMaybe() {
            val card = controller.deck!!.attack(0, withoutSpecialBenefits = counterpart.dead)
            Assert.assertFalse(card.stun)
            if (!counterpart.dead) {
                counterpart.getAttacked(card, controller.player!!)
            }
            val oldValue = card.value
            card.value = Integer.max(0, card.value - 2)
            Assert.assertNotEquals(card.value, oldValue)
            enemy.getAttacked(card, controller.player!!)
        }

        pipis.controller.deck!!.drawPile = mutableListOf(
            Card(value=2, muddle = true),
            Card(value=2, stun = true)
        )

        Assert.assertEquals(enemy, counterpart)
        attackMaybe()
        Assert.assertEquals(4, enemy.taken)
        Assert.assertEquals(6, counterpart.taken)
        Assert.assertNotEquals(enemy, counterpart)
        Assert.assertTrue(counterpart.dead)
        Assert.assertFalse(enemy.dead)
        Assert.assertTrue(enemy.muddled)

        attackMaybe()
        Assert.assertNotEquals(enemy, counterpart)
        Assert.assertFalse(enemy.stunned)
    }

    @Before
    fun setUp() {
        controller = Controller.newFullyStocked()
        pipis = Pipis(controller)
    }
}