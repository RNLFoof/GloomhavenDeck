package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test

internal class CardTest {
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