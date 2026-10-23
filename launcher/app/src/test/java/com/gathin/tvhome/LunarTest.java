package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LunarTest {

    @Test
    public void convertsKnownSolarDatesToLunar() {
        // Today 23 October 2026 -> 14/9 (Âm lịch)
        int[] d1 = Lunar.toLunar(23, 10, 2026, 7.0);
        assertEquals(14, d1[0]);
        assertEquals(9, d1[1]);
        assertEquals(0, d1[3]); // Not leap

        // Tết Giáp Thìn: 10 February 2024 -> 1/1
        int[] d2 = Lunar.toLunar(10, 2, 2024, 7.0);
        assertEquals(1, d2[0]);
        assertEquals(1, d2[1]);
        assertEquals(0, d2[3]);

        // Tết Bính Ngọ: 17 February 2026 -> 1/1
        int[] d3 = Lunar.toLunar(17, 2, 2026, 7.0);
        assertEquals(1, d3[0]);
        assertEquals(1, d3[1]);
        assertEquals(0, d3[3]);
    }
}
