package com.gathin.tvhome;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KidsLockManagerTest {

    @Test
    public void expectedPinFollowsDdMmHhFormat() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // 23 October, 16:00 -> "231016"
        cal.set(2026, Calendar.OCTOBER, 23, 16, 45, 0);
        assertEquals("231016", KidsLockManager.getExpectedPin(cal));

        // 5 March, 08:00 (single digit day, month, hour) -> "050308"
        cal.set(2026, Calendar.MARCH, 5, 8, 15, 0);
        assertEquals("050308", KidsLockManager.getExpectedPin(cal));

        // 1 January, 00:00 (midnight) -> "010100"
        cal.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("010100", KidsLockManager.getExpectedPin(cal));

        // 31 December, 23:59 -> "311223"
        cal.set(2026, Calendar.DECEMBER, 31, 23, 59, 59);
        assertEquals("311223", KidsLockManager.getExpectedPin(cal));
    }

    @Test
    public void multiPressDetectorTriggersOnFifthPressWithinWindow() {
        KidsLockManager manager = new KidsLockManager(null);

        long t0 = 1000L;
        assertFalse(manager.registerTriggerPress(t0));         // 1
        assertFalse(manager.registerTriggerPress(t0 + 400));   // 2
        assertFalse(manager.registerTriggerPress(t0 + 800));   // 3
        assertFalse(manager.registerTriggerPress(t0 + 1200));  // 4
        assertTrue(manager.registerTriggerPress(t0 + 1600));   // 5 -> Triggered!

        // After trigger, counter resets
        assertFalse(manager.registerTriggerPress(t0 + 2000));  // 1 again
    }

    @Test
    public void multiPressDetectorResetsWhenPressesAreTooSlow() {
        KidsLockManager manager = new KidsLockManager(null);

        long t0 = 1000L;
        assertFalse(manager.registerTriggerPress(t0));         // 1
        assertFalse(manager.registerTriggerPress(t0 + 500));   // 2

        // Wait longer than 3000ms window
        long tLater = t0 + 500 + KidsLockManager.MULTI_PRESS_WINDOW_MS + 100L;
        assertFalse(manager.registerTriggerPress(tLater));     // resets to 1!

        // Press 3 more times (total 4) -> should not trigger yet
        assertFalse(manager.registerTriggerPress(tLater + 300)); // 2
        assertFalse(manager.registerTriggerPress(tLater + 600)); // 3
        assertFalse(manager.registerTriggerPress(tLater + 900)); // 4
        assertTrue(manager.registerTriggerPress(tLater + 1200)); // 5 -> Triggered!
    }

    @Test
    public void triggerKeysIncludeMuteOkPower() {
        KidsLockManager manager = new KidsLockManager(null);
        assertTrue(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_VOLUME_MUTE));
        assertTrue(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_MUTE));
        assertTrue(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_DPAD_CENTER));
        assertTrue(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_ENTER));
        assertTrue(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_POWER));
        assertTrue(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_TV_POWER));
        assertTrue(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));

        // Non-trigger keys should not trigger 5-press
        assertFalse(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_A));
        assertFalse(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_DPAD_LEFT));
        assertFalse(manager.isTriggerKey(android.view.KeyEvent.KEYCODE_1));
    }
}
