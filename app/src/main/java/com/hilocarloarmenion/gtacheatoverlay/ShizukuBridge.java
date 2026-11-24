package com.hilocarloarmenion.gtacheatoverlay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import rikka.shizuku.Shizuku;

/**
 * Binds to a Shizuku UserService at shell/root UID; sends only whitelisted
 * Rust-validated mobile cheats. No AccessibilityService and no root exploits.
 * The UI only reports SENT, never claims the cheat took effect in GTA SA.
 */
final class ShizukuBridge {
    interface Result { void onResult(String message); }

    private static final int REQUEST_CODE = 7340;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static volatile ICheatInput remote;
    private static volatile boolean binding;
    private static boolean initialized;
    private static Context app;

    private static final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            remote = ICheatInput.Stub.asInterface(binder);
            binding = false;
            Log.i("GTACheatInjector","Shizuku privileged input service connected");
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            remote = null; binding = false;
        }
    };
    private static final Shizuku.OnBinderReceivedListener binderListener =
        () -> bindIfPermitted();
    private static final Shizuku.OnBinderDeadListener deadListener =
        () -> { remote = null; binding = false; };
    private static final Shizuku.OnRequestPermissionResultListener permissionListener =
        (requestCode, grantResult) -> {
            if (requestCode == REQUEST_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
                bindIfPermitted();
            }
        };

    static synchronized void initialize(Context ctx) {
        if (initialized) return;
        app = ctx.getApplicationContext();
        initialized = true;
        Shizuku.addBinderReceivedListenerSticky(binderListener);
        Shizuku.addBinderDeadListener(deadListener);
        Shizuku.addRequestPermissionResultListener(permissionListener);
    }

    private static boolean permitted() {
        try {
            return Shizuku.pingBinder()
                && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) { return false; }
    }

    static String status(Context ctx) {
        initialize(ctx);
        if (!Shizuku.pingBinder()) return "Shizuku not running";
        if (!permitted()) return "Shizuku permission required";
        if (remote != null) return "Automatic input ready";
        bindIfPermitted();
        return binding ? "Connecting to Shizuku..." : "Shizuku ready to connect";
    }

    static void requestPermission(Activity activity) {
        initialize(activity);
        try {
            if (!Shizuku.pingBinder()) {
                android.widget.Toast.makeText(activity,
                    "Start Shizuku using Wireless debugging first", android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            if (permitted()) {
                bindIfPermitted();
                android.widget.Toast.makeText(activity,
                    "Shizuku authorized. Check status before using the overlay.",
                    android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            if (!Shizuku.shouldShowRequestPermissionRationale())
                Shizuku.requestPermission(REQUEST_CODE);
            else android.widget.Toast.makeText(activity,
                "Allow GTA Cheat Overlay in Shizuku settings", android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.widget.Toast.makeText(activity,
                "Shizuku unavailable: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private static synchronized void bindIfPermitted() {
        if (!initialized || remote != null || binding || !permitted()) return;
        binding = true;
        try {
            Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(
                new ComponentName(app.getPackageName(), InputInjectUserService.class.getName()))
                .processNameSuffix("gta_cheat_input")
                .tag("gta-cheat-input")
                .daemon(false)
                .version(2);
            Shizuku.bindUserService(args,connection);
        } catch (Exception e) {
            binding = false;
            Log.e("GTACheatInjector","Shizuku bind failed",e);
        }
    }

    static void send(Context ctx, String code, Result callback) {
        initialize(ctx);
        if (!NativeCatalog.isKnownCheat(code)) {
            MAIN.post(() -> callback.onResult("ERROR: Code not in original Android cheat catalog"));
            return;
        }
        if (!permitted()) {
            MAIN.post(() -> callback.onResult("ERROR: Start Shizuku and grant permission in the app"));
            return;
        }
        if (remote == null) {
            bindIfPermitted();
            MAIN.post(() -> callback.onResult("ERROR: Shizuku connecting; tap again shortly"));
            return;
        }
        if (!BUSY.compareAndSet(false,true)) {
            MAIN.post(() -> callback.onResult("ERROR: Another cheat is already sending"));
            return;
        }
        new Thread(() -> {
            String result;
            try {
                // Allow the floating menu to close and any underlying game focus to settle.
                Thread.sleep(250);
                ICheatInput target = remote;
                result = target == null ? "ERROR: Shizuku connection lost" : target.sendCode(code);
            } catch (RemoteException ex) {
                result = "ERROR: Shizuku connection lost";
                remote = null; binding = false;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                result = "ERROR: Input interrupted";
            } catch (Exception ex) {
                result = "ERROR: " + ex.getClass().getSimpleName();
            } finally {
                BUSY.set(false);
            }
            String reply = result;
            MAIN.post(() -> callback.onResult(reply));
        }, "GTA-Shizuku-Input").start();
    }

    private ShizukuBridge() {}
}
