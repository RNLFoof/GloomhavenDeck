package com.example.gloomhavendeck


import org.junit.Assert
import org.junit.Test

internal class StatusTest {
    @Test
    fun getNextAutomaticPosition() {
        val roundBasedStatus = Status.values().first {it.roundBased}
        Assert.assertEquals(0, roundBasedStatus.getNextAutomaticPosition(0))
        Assert.assertEquals(0, roundBasedStatus.getNextAutomaticPosition(1))
        Assert.assertEquals(1, roundBasedStatus.getNextAutomaticPosition(2))

        val notRoundBasedStatus = Status.values().first {!it.roundBased}
        Assert.assertEquals(0, notRoundBasedStatus.getNextAutomaticPosition(0))
        Assert.assertEquals(1, notRoundBasedStatus.getNextAutomaticPosition(1))
    }
}