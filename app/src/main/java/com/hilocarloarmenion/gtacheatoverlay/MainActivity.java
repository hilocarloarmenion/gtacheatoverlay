package com.hilocarloarmenion.gtacheatoverlay;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;
    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(25),dp(20),dp(25),dp(20));
        root.setBackgroundColor(Color.rgb(19,17,30));
        label(root, "GTA CHEAT OVERLAY", 25, true, 0xFFEAE7FF);
        label(root, "Original GTA SA Android 2.10", 15, false, 0xFFBBACE8);
        label(root, "Floating circle → Tier 1 → Tier 2 → Tier 3. A compact cheat browser built around Rust.", 16, false, Color.WHITE);
        status = label(root, "", 15, false, 0xFFB5B4C5);
        button(root, "1. Allow display over other apps", () -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        button(root, "2. Show floating circle", () -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Enable overlay permission first", Toast.LENGTH_LONG).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 123);
            }
            try {
                Intent service = new Intent(this, OverlayService.class);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
                else startService(service);
                Toast.makeText(this, "Bubble launched. Open GTA SA.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Unable to start overlay: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        button(root, "Stop overlay", () -> stopService(new Intent(this, OverlayService.class)));
        label(root, "This prototype displays and copies MOBILE cheat codes. It does not yet inject input into GTA. Use the virtual keyboard you already tested to enter copied codes.", 13, false, 0xFFFFC879);
        setContentView(root);
    }
    private TextView label(LinearLayout root, String text, int sp, boolean bold, int color) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextSize(sp); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        v.setPadding(0,0,0,dp(20)); root.addView(v); return v;
    }
    private void button(LinearLayout root, String text, Runnable click) {
        Button b = new Button(this);
        b.setText(text); b.setAllCaps(false); b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6643B8));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,dp(53));
        p.bottomMargin=dp(12); root.addView(b,p);
        b.setOnClickListener(v -> click.run());
    }
    @Override protected void onResume() {
        super.onResume();
        if(status!=null) status.setText("Overlay permission: "
            + (Settings.canDrawOverlays(this)?"Granted ✓":"Not granted"));
    }
}
