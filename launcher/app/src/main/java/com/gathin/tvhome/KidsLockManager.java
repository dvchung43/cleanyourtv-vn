package com.gathin.tvhome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

/**
 * Handles Kids Mode screen-time countdown, persistent lock state across reboots,
 * dynamic ddMMHH PIN generation, 5-press trigger detection, and curtain screen overlay.
 */
public final class KidsLockManager {

    private static final String PREF_NAME = "kids_mode_prefs";
    private static final String KEY_ACTIVE = "kids_active";
    private static final String KEY_EXPIRE_AT = "kids_expire_at";
    private static final String KEY_LOCKED = "kids_locked";

    static final long MULTI_PRESS_WINDOW_MS = 3000L;
    static final int REQUIRED_PRESSES = 5;

    private final MainActivity activity;
    private final SharedPreferences prefs;
    private final AudioManager audioManager;

    private int pressCount = 0;
    private long lastPressTime = 0;
    private final StringBuilder enteredPin = new StringBuilder();

    private View lockOverlay;
    private View pinContainer;
    private TextView pinDots;
    private TextView pinError;
    private GridLayout keypad;

    public KidsLockManager(MainActivity activity) {
        this.activity = activity;
        this.prefs = activity != null ? activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) : null;
        this.audioManager = activity != null ? (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE) : null;
    }

    /** Returns dynamic 6-digit PIN in ddMMHH format (e.g. 23 Oct at 16:00 -> "231016"). */
    public static String getExpectedPin(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int hour24 = cal.get(Calendar.HOUR_OF_DAY);
        return String.format(Locale.US, "%02d%02d%02d", day, month, hour24);
    }

    public static String getExpectedPin() {
        return getExpectedPin(Calendar.getInstance());
    }

    /** Detects 5 consecutive trigger key presses within MULTI_PRESS_WINDOW_MS. */
    public boolean registerTriggerPress(long nowUptimeMs) {
        if (nowUptimeMs - lastPressTime > MULTI_PRESS_WINDOW_MS) {
            pressCount = 1;
        } else {
            pressCount++;
        }
        lastPressTime = nowUptimeMs;
        if (pressCount >= REQUIRED_PRESSES) {
            pressCount = 0;
            return true;
        }
        return false;
    }

    public void resetPressCount() {
        pressCount = 0;
        lastPressTime = 0;
    }

    public boolean isActive() {
        return prefs != null && prefs.getBoolean(KEY_ACTIVE, false);
    }

    public long getExpireAt() {
        return prefs != null ? prefs.getLong(KEY_EXPIRE_AT, 0L) : 0L;
    }

    public boolean isLocked() {
        if (!isActive() || prefs == null) return false;
        if (prefs.getBoolean(KEY_LOCKED, false)) return true;
        long expireAt = getExpireAt();
        if (expireAt > 0 && System.currentTimeMillis() >= expireAt) {
            prefs.edit().putBoolean(KEY_LOCKED, true).apply();
            return true;
        }
        return false;
    }

    public int getRemainingMinutes() {
        if (!isActive()) return 0;
        long diff = getExpireAt() - System.currentTimeMillis();
        return Math.max(0, (int) Math.ceil(diff / 60_000.0));
    }

    public void start(int durationMinutes) {
        long expireAt = System.currentTimeMillis() + durationMinutes * 60_000L;
        prefs.edit()
                .putBoolean(KEY_ACTIVE, true)
                .putBoolean(KEY_LOCKED, false)
                .putLong(KEY_EXPIRE_AT, expireAt)
                .apply();
        scheduleAlarm(expireAt);
        updateLockState();
    }

    public void stop() {
        prefs.edit()
                .putBoolean(KEY_ACTIVE, false)
                .putBoolean(KEY_LOCKED, false)
                .putLong(KEY_EXPIRE_AT, 0L)
                .apply();
        cancelAlarm();
        muteAudio(false);
        if (lockOverlay != null) {
            lockOverlay.setVisibility(View.GONE);
        }
        if (pinContainer != null) {
            pinContainer.setVisibility(View.GONE);
        }
        resetPressCount();
        if (activity != null) {
            activity.updateKidsButtonState();
        }
    }

    private void scheduleAlarm(long triggerAtMillis) {
        try {
            AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent pi = PendingIntent.getActivity(activity, 1001, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            }
        } catch (Throwable ignored) {
        }
    }

    private void cancelAlarm() {
        try {
            AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                Intent intent = new Intent(activity, MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(activity, 1001, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.cancel(pi);
            }
        } catch (Throwable ignored) {
        }
    }

    public void bindViews(View overlay, View container, TextView dots, TextView error, GridLayout grid) {
        this.lockOverlay = overlay;
        this.pinContainer = container;
        this.pinDots = dots;
        this.pinError = error;
        this.keypad = grid;

        buildKeypad();
    }

    public void updateLockState() {
        if (lockOverlay == null) return;
        if (isLocked()) {
            lockOverlay.setBackgroundColor(0xFF000000);
            lockOverlay.setVisibility(View.VISIBLE);
            muteAudio(true);
        } else {
            if (pinContainer == null || pinContainer.getVisibility() != View.VISIBLE) {
                lockOverlay.setVisibility(View.GONE);
            }
            muteAudio(false);
        }
    }

    private void muteAudio(boolean mute) {
        if (audioManager == null) return;
        try {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
        } catch (Throwable ignored) {
        }
    }

    public void showPinDialog() {
        if (pinContainer == null || lockOverlay == null) return;
        enteredPin.setLength(0);
        updatePinDots();
        if (pinError != null) pinError.setVisibility(View.INVISIBLE);
        if (isLocked()) {
            lockOverlay.setBackgroundColor(0xFF000000);
        } else {
            lockOverlay.setBackgroundColor(0xCC000000);
        }
        lockOverlay.setVisibility(View.VISIBLE);
        pinContainer.setVisibility(View.VISIBLE);

        // Focus first key
        if (keypad != null && keypad.getChildCount() > 0) {
            keypad.getChildAt(0).requestFocus();
        }
    }

    public void hidePinDialog() {
        if (pinContainer != null) {
            pinContainer.setVisibility(View.GONE);
        }
        if (lockOverlay != null) {
            if (isLocked()) {
                lockOverlay.setBackgroundColor(0xFF000000);
                lockOverlay.setVisibility(View.VISIBLE);
            } else {
                lockOverlay.setVisibility(View.GONE);
            }
        }
        enteredPin.setLength(0);
        if (pinError != null) pinError.setVisibility(View.INVISIBLE);
    }

    private void buildKeypad() {
        if (keypad == null || keypad.getChildCount() > 0) return;

        String[] labels = {
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                "⌫", "0", "✕"
        };

        float density = activity.getResources().getDisplayMetrics().density;
        int btnWidth = (int) (80 * density);
        int btnHeight = (int) (52 * density);
        int margin = (int) (4 * density);

        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            TextView btn = new TextView(activity);
            btn.setId(View.generateViewId());
            btn.setText(label);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            btn.setGravity(Gravity.CENTER);
            btn.setFocusable(true);
            btn.setClickable(true);
            btn.setBackgroundResource(R.drawable.pill_bg);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = btnWidth;
            lp.height = btnHeight;
            lp.setMargins(margin, margin, margin, margin);
            btn.setLayoutParams(lp);

            btn.setOnClickListener(v -> onKeypadClick(label));
            keypad.addView(btn);
        }

        // Trap focus inside the 3x4 keypad so D-pad cannot escape to background views
        for (int i = 0; i < keypad.getChildCount(); i++) {
            View child = keypad.getChildAt(i);
            int row = i / 3;
            int col = i % 3;
            if (row == 0) child.setNextFocusUpId(child.getId());
            if (row == 3) child.setNextFocusDownId(child.getId());
            if (col == 0) child.setNextFocusLeftId(child.getId());
            if (col == 2) child.setNextFocusRightId(child.getId());
        }
    }

    private void onKeypadClick(String label) {
        if ("✕".equals(label)) {
            hidePinDialog();
        } else if ("⌫".equals(label)) {
            if (enteredPin.length() > 0) {
                enteredPin.deleteCharAt(enteredPin.length() - 1);
                updatePinDots();
                if (pinError != null) pinError.setVisibility(View.INVISIBLE);
            }
        } else {
            if (enteredPin.length() < 6) {
                enteredPin.append(label);
                updatePinDots();
                if (enteredPin.length() == 6) {
                    verifyPin();
                }
            }
        }
    }

    private void updatePinDots() {
        if (pinDots == null) return;
        StringBuilder sb = new StringBuilder();
        int len = enteredPin.length();
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append("   ");
            sb.append(i < len ? "●" : "○");
        }
        pinDots.setText(sb.toString());
    }

    private void verifyPin() {
        String expected = getExpectedPin();
        if (enteredPin.toString().equals(expected)) {
            stop();
            Toast.makeText(activity, R.string.kids_mode_unlocked, Toast.LENGTH_SHORT).show();
        } else {
            if (pinError != null) pinError.setVisibility(View.VISIBLE);
            enteredPin.setLength(0);
            updatePinDots();
        }
    }

    public boolean isTriggerKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_POWER
                || keyCode == KeyEvent.KEYCODE_TV_POWER
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_MUTE
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
    }

    /** Intercepts key events while TV is locked under curtain mode or entering PIN. */
    public boolean handleLockedKeyEvent(KeyEvent event) {
        // When PIN dialog is visible, handle numeric keys, backspace and back
        if (pinContainer != null && pinContainer.getVisibility() == View.VISIBLE) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int code = event.getKeyCode();
                if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) {
                    onKeypadClick(String.valueOf(code - KeyEvent.KEYCODE_0));
                    return true;
                }
                if (code >= KeyEvent.KEYCODE_NUMPAD_0 && code <= KeyEvent.KEYCODE_NUMPAD_9) {
                    onKeypadClick(String.valueOf(code - KeyEvent.KEYCODE_NUMPAD_0));
                    return true;
                }
                if (code == KeyEvent.KEYCODE_DEL) {
                    onKeypadClick("⌫");
                    return true;
                }
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                hidePinDialog();
                return true;
            }
            return false; // allow D-pad navigation between keys
        }

        if (!isLocked()) {
            return false;
        }

        // When curtain overlay is completely black:
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0 && isTriggerKey(event.getKeyCode())) {
            boolean triggered = registerTriggerPress(SystemClock.uptimeMillis());
            if (triggered) {
                showPinDialog();
            }
        }
        return true; // block all other key actions
    }
}
