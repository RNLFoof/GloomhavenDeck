package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test

internal class CardTest {
    @Test
    fun withoutSpecialBenefits() {
        val interestingCard = Card(value=2, pierce = 3, stun = true)
        val boringCard = Card(value=2)
        Assert.assertEquals(boringCard, boringCard.withoutSpecialBenefits())
        Assert.assertNotEquals(interestingCard, interestingCard.withoutSpecialBenefits())
    }

    @Test
    fun hasSpecialBenefits() {
        Assert.assertTrue(Card(stun=true).hasSpecialBenefits())
        Assert.assertFalse(Card(lose=true).hasSpecialBenefits())
        Assert.assertFalse(Card().hasSpecialBenefits())
    }

    @Test
    fun plus() {
        Assert.assertNotEquals(
            Card(value=2),
            Card(value=3)
        )

        Assert.assertNotEquals(
            Card(value=2),
            Card(value=2, multiplier = true)
        )

        Assert.assertEquals(
            Card(value=6),
            Card(value=3) + Card(value=2, multiplier = true)
        )
    }
}