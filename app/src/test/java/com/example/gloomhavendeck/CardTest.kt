package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test

internal class CardTest {
    @Test
    fun withoutAllButValue() {
        val interestingCard = Card(value=2, pierce = 3)
        val boringCard = Card(value=2)
        val multiplierCard = Card(multiplier = true)
        Assert.assertEquals(boringCard, boringCard.withoutAllButValue())
        Assert.assertNotEquals(interestingCard, interestingCard.withoutAllButValue())
        Assert.assertThrows(Exception::class.java) {multiplierCard.withoutAllButValue()}
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