package com.hilocarloarmenion.gtacheatoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.ref.WeakReference;

/**
 * Standalone, user-enabled Android input method. No Shizuku, root, extra app,
 * hidden APIs, AccessibilityService, or shell injection.
 *
 * InputConnection.sendKeyEvent sends real Android key-down/up events to the
 * app attached to this keyboard. GTA SA 2.10 must expose an active input
 * connection for this approach to work. A successful dispatch does not prove
 * the game enabled the cheat. This limitation is intentional and visible.
 */
public final class GtaKeyboardService extends InputMethodService {
    public interface Result { void onResult(String result); }
    private static final Handler UI = new Handler(Looper.getMainLooper());
    private static WeakReference<GtaKeyboardService> current = new WeakReference<>(null);
    private boolean sending;

    private int dp(int value) {
        return (int)(getResources().getDisplayMetrics().density * value + .5f);
    }

    @Override public void onCreate() {
        super.onCreate();
        current = new WeakReference<>(this);
    }

    @Override public void onDestroy() {
        if (current.get() == this) current = new WeakReference<>(null);
        super.onDestroy();
    }

    private boolean isGameFocused() {
        EditorInfo info = getCurrentInputEditorInfo();
        if (info == null || info.packageName == null) return false;
        String packageName = info.packageName.toLowerCase(java.util.Locale.ROOT);
        return packageName.equals("com.rockstargames.gtasa")
            || packageName.endsWith(".gtasa");
    }

    public static String connectionStatus() {
        GtaKeyboardService service = current.get();
        if (service == null) return "Built-in keyboard not active";
        if (!service.isGameFocused()) return "Keyboard not attached to GTA SA";
        if (service.getCurrentInputConnection() == null) return "No game input connection";
        return "Game keyboard connected";
    }

    public static void requestKeyboard() {
        GtaKeyboardService service = current.get();
        if (service != null) UI.post(() -> {
            try { service.requestShowSelf(0); } catch (RuntimeException ignored) {}
        });
    }

    public static void sendCode(String code, Runnable beforeSending, Result callback) {
        UI.post(() -> {
            GtaKeyboardService service = current.get();
            if (service == null) {
                callback.onResult("Keyboard unavailable. Enable and select the built-in GTA keyboard first.");
                return;
            }
            if (!NativeCatalog.isKnownCheat(code)) {
                callback.onResult("Invalid Android GTA cheat code.");
                return;
            }
            if (!service.isGameFocused()) {
                callback.onResult("GTA has no active keyboard connection. Try opening the built-in keyboard while in GTA.");
                return;
            }
            InputConnection connection = service.getCurrentInputConnection();
            if (connection == null) {
                callback.onResult("GTA has not supplied an input connection. Code was NOT sent.");
                return;
            }
            if (service.sending) {
                callback.onResult("Previous cheat is still being entered.");
                return;
            }
            service.sending = true;
            beforeSending.run();
            // Each letter is sent separately, as with a human pressing a keyboard.
            // Keep a gap between events for the older GTA SA key handler.
            service.sendNext(code, 0, callback);
        });
    }

    private void sendNext(String code, int i, Result callback) {
        if (i >= code.length()) {
            sending = false;
            callback.onResult("Sent " + code.length()
                + " keyboard keypresses. Check GTA to confirm the cheat activated.");
            return;
        }
        if (!isGameFocused()) {
            sending = false;
            callback.onResult("Input focus left GTA. Stopped before the full code was entered.");
            return;
        }
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            sending = false;
            callback.onResult("GTA input connection disconnected. Code not completed.");
            return;
        }
        int keyCode = KeyEvent.KEYCODE_A + (code.charAt(i) - 'A');
        long t = android.os.SystemClock.uptimeMillis();
        int flags = KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE;
        KeyEvent down = new KeyEvent(t,t,KeyEvent.ACTION_DOWN,keyCode,0,0,
            KeyCharacterMap.VIRTUAL_KEYBOARD,0,flags,InputDevice.SOURCE_KEYBOARD);
        boolean accepted = connection.sendKeyEvent(down);
        UI.postDelayed(() -> {
            InputConnection currentConnection = getCurrentInputConnection();
            if (currentConnection == null) {
                sending = false;
                callback.onResult("Input connection lost during cheat sequence.");
                return;
            }
            long now = android.os.SystemClock.uptimeMillis();
            KeyEvent up = new KeyEvent(t,now,KeyEvent.ACTION_UP,keyCode,0,0,
                KeyCharacterMap.VIRTUAL_KEYBOARD,0,flags,InputDevice.SOURCE_KEYBOARD);
            boolean released = currentConnection.sendKeyEvent(up);
            if (!accepted || !released) {
                sending = false;
                callback.onResult("GTA rejected a keyboard event; stopped sending.");
            } else {
                UI.postDelayed(() -> sendNext(code, i + 1, callback), 85);
            }
        }, 35);
    }

    // Don't replace landscape GTA with an Android fullscreen text editor.
    @Override public boolean onEvaluateFullscreenMode() {
        return false;
    }

    private void sendManualKey(int keyCode) {
        InputConnection conn = getCurrentInputConnection();
        if (conn == null) return;
        long t = android.os.SystemClock.uptimeMillis();
        int flags = KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE;
        conn.sendKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN,
                keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, flags, InputDevice.SOURCE_KEYBOARD));
        conn.sendKeyEvent(new KeyEvent(t, android.os.SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD,
                0, flags, InputDevice.SOURCE_KEYBOARD));
    }

    private void addActionKey(LinearLayout row, String title, int keycode) {
        TextView key = new TextView(this);
        key.setText(title);
        key.setTextSize(14);
        key.setTextColor(Color.WHITE);
        key.setGravity(Gravity.CENTER);
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xFF725397);
        background.setCornerRadius(dp(7));
        key.setBackground(background);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(38), 1);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(key, lp);
        key.setOnClickListener(v -> {
            if (keycode < 0) requestHideSelf(0);
            else sendManualKey(keycode);
        });
    }

    @Override public View onCreateInputView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(4),dp(3),dp(4),dp(4));
        root.setBackgroundColor(0xFF211B32);
        TextView title = new TextView(this);
        title.setText("GTA Cheat Keyboard • sends individual keys");
        title.setTextSize(12);
        title.setTextColor(0xFFD6CAE8);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1,dp(24)));
        for (String letters : new String[]{"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"}) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            for (int i=0; i<letters.length(); i++) {
                char c=letters.charAt(i);
                TextView key=new TextView(this);
                key.setText(String.valueOf(c));
                key.setTextColor(Color.WHITE);
                key.setTextSize(16);
                key.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                key.setGravity(Gravity.CENTER);
                GradientDrawable background = new GradientDrawable();
                background.setColor(0xFF51406D);
                background.setCornerRadius(dp(7));
                key.setBackground(background);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(41),1);
                p.setMargins(dp(2),dp(2),dp(2),dp(2));
                row.addView(key,p);
                key.setOnClickListener(v -> sendManualKey(KeyEvent.KEYCODE_A + (c - 'A')));
            }
            root.addView(row);
        }
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setGravity(Gravity.CENTER);
        addActionKey(actionRow, "SPACE", KeyEvent.KEYCODE_SPACE);
        addActionKey(actionRow, "DEL", KeyEvent.KEYCODE_DEL);
        addActionKey(actionRow, "ENTER", KeyEvent.KEYCODE_ENTER);
        addActionKey(actionRow, "HIDE", -1);
        root.addView(actionRow);
        return root;
    }
}
