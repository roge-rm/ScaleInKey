package com.rm.scaleinkey.music

import org.junit.Assert.assertEquals
import org.junit.Test

class TriadQualityTest {

    @Test
    fun `classifies the four triad types from third and fifth intervals`() {
        assertEquals(TriadQuality.MAJOR, TriadQuality.fromIntervals(4, 7))
        assertEquals(TriadQuality.MINOR, TriadQuality.fromIntervals(3, 7))
        assertEquals(TriadQuality.DIMINISHED, TriadQuality.fromIntervals(3, 6))
        assertEquals(TriadQuality.AUGMENTED, TriadQuality.fromIntervals(4, 8))
    }
}
