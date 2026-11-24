package com.hilocarloarmenion.gtacheatoverlay;

import android.os.Binder;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shizuku UserService: the class is loaded in an isolated Shizuku shell/root
 * process, not as a normal Android Service. It cannot inject without a running
 * authorized Shizuku instance. No root is requested from the app itself.
 *
 * This executes ONLY /system/bin/input keyevent with alphabetic keycodes.
 * Arbitrary shell commands or text values are never accepted.
 */
public final class InputInjectUserService extends ICheatInput.Stub {
    public InputInjectUserService() {}

    @Override
    public synchronized String sendCode(String code) {
        if (code == null || !code.matches("[A-Z]{7,12}"))
            return "ERROR: Invalid mobile cheat code";
        if (Process.myUid() != 2000 && Process.myUid() != 0)
            return "ERROR: Privileged Shizuku shell session not active";

        List<String> args = new ArrayList<>();
        args.add("/system/bin/input");
        args.add("keyevent");
        for (int i = 0; i < code.length(); ++i) {
            args.add("KEYCODE_" + code.charAt(i));
        }

        java.lang.Process proc = null;
        try {
            proc = new ProcessBuilder(args).redirectErrorStream(true).start();
            if (!proc.waitFor(8, TimeUnit.SECONDS)) {
                proc.destroyForcibly();
                return "ERROR: System input command timed out";
            }
            if (proc.exitValue() != 0) {
                return "ERROR: System input refused key events (exit " + proc.exitValue() + ")";
            }
            return "SENT: " + code.length() + " keyboard key events";
        } catch (Exception ex) {
            Log.e("GTACheatInjector","Failed to inject code",ex);
            return "ERROR: " + ex.getClass().getSimpleName();
        } finally {
            if (proc != null) proc.destroy();
        }
    }
}
