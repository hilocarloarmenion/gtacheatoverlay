package com.hilocarloarmenion.gtacheatoverlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class OverlayService extends Service {
    private static final int PURPLE = 0xFFAD79F3, WHITE = 0xFFF6F3FF, MUTED = 0xFFC9BFD8;
    private WindowManager wm;
    private View bubble, panel;
    private WindowManager.LayoutParams bubbleParams;
    private JSONArray categories;
    private int tier=1, selectedCategory=-1, selectedSubcategory=-1;
    private String pendingWarnCode;
    private long pendingWarnAt;
    private int dp(int px) {return (int)(px*getResources().getDisplayMetrics().density+.5f);}
    @Override public IBinder onBind(Intent intent) {return null;}
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("overlay", "GTA Overlay", NotificationManager.IMPORTANCE_LOW));
        Notification notification=new Notification.Builder(this,"overlay")
            .setContentTitle("GTA Cheat Overlay")
            .setContentText("Floating circle is active — tap to browse cheats")
            .setSmallIcon(android.R.drawable.ic_menu_info_details).setOngoing(true).build();
        if (Build.VERSION.SDK_INT>=34) startForeground(1,notification,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(1,notification);
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        try {
            categories=new JSONObject(NativeCatalog.read()).getJSONArray("categories");
            createBubble();
        } catch (Exception e) {
            Toast.makeText(this,"Unable to load cheat data: "+e.getMessage(),Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }
    private GradientDrawable rounded(int color,int radius,int stroke) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1),stroke);
        return d;
    }
    private WindowManager.LayoutParams params(int width,int height,int gravity,int flags) {
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(
            width,height,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,android.graphics.PixelFormat.TRANSLUCENT);
        p.gravity=gravity;
        return p;
    }
    // Split touch lets a second finger tap the bubble while the first finger
    // stays on GTA's joystick, provided the game's own window supports it.
    private static final int INTERACTIVE_FLAGS =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
    private View dropTarget;
    private WindowManager.LayoutParams dropParams;

    private int screenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int screenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    private void createBubble() {
        TextView v = new TextView(this);
        v.setText("🎮");
        v.setTextSize(24);
        v.setGravity(Gravity.CENTER);
        v.setBackground(rounded(0xED53338C, 50, PURPLE));
        v.setElevation(dp(8));
        bubble = v;

        bubbleParams = params(dp(54), dp(54), Gravity.TOP | Gravity.LEFT, INTERACTIVE_FLAGS);
        // The bubble's window is only 54dp wide, not an invisible full-screen
        // overlay. Touches outside it continue to reach GTA.
        bubbleParams.x = Math.max(0, screenWidth() - dp(68));
        bubbleParams.y = Math.max(dp(12), (screenHeight() - dp(54)) / 2);

        v.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int startX, startY;
            int pointerId = -1;
            boolean dragged;

            private float pointerRawX(MotionEvent e, int index) {
                return e.getRawX() + e.getX(index) - e.getX(0);
            }

            private float pointerRawY(MotionEvent e, int index) {
                return e.getRawY() + e.getY(index) - e.getY(0);
            }

            @Override public boolean onTouch(View view, MotionEvent event) {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    pointerId = event.getPointerId(0);
                    downX = event.getRawX();
                    downY = event.getRawY();
                    startX = bubbleParams.x;
                    startY = bubbleParams.y;
                    dragged = false;
                    return true;
                }
                int index = pointerId < 0 ? -1 : event.findPointerIndex(pointerId);
                if (action == MotionEvent.ACTION_CANCEL) {
                    hideDropTarget();
                    pointerId = -1;
                    return true;
                }
                // ACTION_POINTER_UP can happen if more than one finger lands
                // inside the bubble. Only the pointer that started the drag
                // is permitted to move or close it.
                if (action == MotionEvent.ACTION_POINTER_UP
                        && event.getPointerId(event.getActionIndex()) != pointerId) {
                    return true;
                }
                if (index < 0) return true;

                if (action == MotionEvent.ACTION_MOVE) {
                    int dx = Math.round(pointerRawX(event, index) - downX);
                    int dy = Math.round(pointerRawY(event, index) - downY);
                    if (!dragged && Math.abs(dx) + Math.abs(dy) > dp(11)) {
                        dragged = true;
                        hidePanel();
                        showDropTarget();
                    }
                    if (dragged) {
                        bubbleParams.x = Math.max(0,
                                Math.min(screenWidth() - dp(54), startX + dx));
                        bubbleParams.y = Math.max(0,
                                Math.min(screenHeight() - dp(54), startY + dy));
                        wm.updateViewLayout(bubble, bubbleParams);
                        updateDropTargetHover(isOverDropTarget());
                    }
                    return true;
                }

                if (action == MotionEvent.ACTION_UP
                        || (action == MotionEvent.ACTION_POINTER_UP
                        && event.getPointerId(event.getActionIndex()) == pointerId)) {
                    pointerId = -1;
                    if (!dragged) {
                        togglePanel();
                    } else {
                        boolean close = isOverDropTarget();
                        hideDropTarget();
                        if (close) {
                            stopSelf(); // Only close our bubble/service, not GTA.
                        } else {
                            // Snap to the edge so the bubble doesn't sit on a
                            // gameplay control after being moved.
                            bubbleParams.x = bubbleParams.x + dp(27) < screenWidth() / 2
                                    ? dp(8)
                                    : Math.max(0, screenWidth() - dp(62));
                            wm.updateViewLayout(bubble, bubbleParams);
                            getSharedPreferences("overlay", MODE_PRIVATE).edit()
                                    .putFloat("bubble_y", (float) bubbleParams.y
                                            / Math.max(1, screenHeight() - dp(54)))
                                    .apply();
                        }
                    }
                    return true;
                }
                return true;
            }
        });
        float savedY = getSharedPreferences("overlay", MODE_PRIVATE).getFloat("bubble_y", -1);
        if (savedY >= 0) {
            bubbleParams.y = Math.max(0, Math.min(screenHeight() - dp(54),
                    Math.round(savedY * (screenHeight() - dp(54)))));
        }
        wm.addView(bubble, bubbleParams);
    }

    private void showDropTarget() {
        if (dropTarget != null) return;
        TextView x = new TextView(this);
        x.setText("×");
        x.setTextSize(38);
        x.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        x.setTextColor(WHITE);
        x.setGravity(Gravity.CENTER);
        x.setBackground(rounded(0xE66D374A, 90, 0xFFBA7180));
        dropTarget = x;
        // Visual indicator only; it must never steal touches from the game.
        dropParams = params(dp(76), dp(76), Gravity.TOP | Gravity.LEFT,
                INTERACTIVE_FLAGS | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dropParams.alpha = 0.6f;
        dropParams.x = Math.max(0, (screenWidth() - dp(76)) / 2);
        dropParams.y = Math.max(0, screenHeight() - dp(96));
        wm.addView(dropTarget, dropParams);
    }

    private boolean isOverDropTarget() {
        if (dropTarget == null || dropParams == null) return false;
        int dx = bubbleParams.x + dp(27) - (dropParams.x + dp(38));
        int dy = bubbleParams.y + dp(27) - (dropParams.y + dp(38));
        return dx * dx + dy * dy <= dp(55) * dp(55);
    }

    private void updateDropTargetHover(boolean hovering) {
        if (dropTarget != null) {
            dropTarget.setBackground(rounded(hovering ? 0xFFE44862 : 0xE66D374A,
                    90, hovering ? 0xFFFFFFFF : 0xFFBA7180));
        }
    }

    private void hideDropTarget() {
        if (dropTarget != null) {
            wm.removeView(dropTarget);
            dropTarget = null;
            dropParams = null;
        }
    }

    private void togglePanel() {
        if(panel!=null){hidePanel();return;}
        tier=1;selectedCategory=-1;selectedSubcategory=-1;showPanel();
    }
    private void hidePanel() {
        if(panel!=null){wm.removeView(panel);panel=null;}
    }
    private void showPanel() {
        hidePanel();
        int width=Math.min(dp(360),getResources().getDisplayMetrics().widthPixels-dp(24));
        int height=Math.min(dp(430),getResources().getDisplayMetrics().heightPixels-dp(40));
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(12),dp(12),dp(12));
        root.setBackground(rounded(0xF0201C2C,22,0xFF6E4D9E));
        root.setElevation(dp(12));

        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        if(tier>1){
            TextView back=text("‹",32,WHITE,true);
            back.setGravity(Gravity.CENTER);
            header.addView(back,new LinearLayout.LayoutParams(dp(37),dp(43)));
            back.setOnClickListener(v->back());
        }
        LinearLayout titleBox=new LinearLayout(this);titleBox.setOrientation(LinearLayout.VERTICAL);
        String title="Cheat Overlay";
        try {
            if(tier==2)title=categories.getJSONObject(selectedCategory).getString("name");
            else if(tier==3) title=categories.getJSONObject(selectedCategory)
                .getJSONArray("subcategories").getJSONObject(selectedSubcategory).getString("name");
        }catch(Exception ignored){}
        titleBox.addView(text(title,17,WHITE,true));
        titleBox.addView(text("Tier "+tier,12,PURPLE,false));
        header.addView(titleBox,new LinearLayout.LayoutParams(0,dp(49),1));
        TextView close=text("×",28,WHITE,false);
        close.setGravity(Gravity.CENTER);
        header.addView(close,new LinearLayout.LayoutParams(dp(38),dp(43)));
        close.setOnClickListener(v->hidePanel());
        root.addView(header);
        View line=new View(this);line.setBackgroundColor(0xFF614379);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));
        lp.bottomMargin=dp(10);root.addView(line,lp);

        ScrollView scroller=new ScrollView(this);
        scroller.setFillViewport(false);scroller.setVerticalScrollBarEnabled(false);
        LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);
        scroller.addView(rows);
        root.addView(scroller,new LinearLayout.LayoutParams(-1,0,1));
        try {
            if(tier==1) {
                for(int i=0;i<categories.length();i++){
                    JSONObject item=categories.getJSONObject(i);
                    final int index=i;
                    addRow(rows,item.getString("name"),"›",()->{selectedCategory=index;tier=2;showPanel();},false);
                }
            } else if(tier==2) {
                JSONArray subs=categories.getJSONObject(selectedCategory).getJSONArray("subcategories");
                for(int i=0;i<subs.length();i++) {
                    final int index=i;
                    addRow(rows,subs.getJSONObject(i).getString("name"),"›",
                        ()->{selectedSubcategory=index;tier=3;showPanel();},false);
                }
            } else {
                addRow(rows,"Open built-in keyboard","⌨", ()->{hidePanel();GtaKeyboardService.requestKeyboard();},false);
                JSONArray cheats=categories.getJSONObject(selectedCategory).getJSONArray("subcategories")
                    .getJSONObject(selectedSubcategory).getJSONArray("cheats");
                for(int i=0;i<cheats.length();i++) {
                    JSONObject c=cheats.getJSONObject(i);
                    String name=c.getString("name"),code=c.getString("code");
                    String warning=c.optString("warning","");
                    addRow(rows,name+"\n"+code+(warning.isEmpty()?"":"\n⚠ "+warning),
                        "⚡",()->injectCheat(code,warning),true)
                        .setOnLongClickListener(v->{copyCode(code);return true;});
                }
            }
        }catch(Exception e) {rows.addView(text("Error reading categories",14,WHITE,false));}
        TextView footer=text(tier==3?"Tap = type keys • Hold = copy • "+GtaKeyboardService.connectionStatus()
            :"GTA SA original Android v2.10",11,MUTED,false);
        footer.setGravity(Gravity.CENTER);footer.setPadding(0,dp(8),0,0);
        root.addView(footer);
        WindowManager.LayoutParams p=params(width,height,
                Gravity.RIGHT | Gravity.CENTER_VERTICAL, INTERACTIVE_FLAGS);
        p.x=dp(8);panel=root;wm.addView(panel,p);
    }
    private TextView text(String text,int sp,int color,boolean bold) {
        TextView view=new TextView(this);view.setText(text);
        view.setTextSize(sp);view.setTextColor(color);
        if(bold)view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return view;
    }
    private LinearLayout addRow(LinearLayout list,String title,String trailing,Runnable action,boolean cheat) {
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12),dp(9),dp(12),dp(9));
        row.setBackground(rounded(0xFF343042,12,0xFF534560));
        TextView text=text(title,cheat?13:15,WHITE,false);
        text.setMaxLines(cheat?5:2);
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        TextView suffix=text(trailing,cheat?20:24,PURPLE,true);
        suffix.setGravity(Gravity.CENTER);
        row.addView(suffix,new LinearLayout.LayoutParams(dp(30),dp(36)));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
        params.bottomMargin=dp(7);list.addView(row,params);
        row.setOnClickListener(v->action.run());
        return row;
    }
    private void injectCheat(String code,String warning) {
        if(!warning.isEmpty()) {
            long now=android.os.SystemClock.uptimeMillis();
            if(!code.equals(pendingWarnCode) || now-pendingWarnAt>6000) {
                pendingWarnCode=code;pendingWarnAt=now;
                Toast.makeText(this,"⚠ "+warning+" — tap again within 6 seconds to send",Toast.LENGTH_LONG).show();
                return;
            }
        }
        pendingWarnCode=null;
        GtaKeyboardService.sendCode(code, this::hidePanel, message -> {
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();
        });
    }
    private void copyCode(String code) {
        ClipboardManager clipboard=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("GTA SA mobile cheat",code));
        Toast.makeText(this,code+" copied — enter with your GTA keyboard",Toast.LENGTH_SHORT).show();
    }
    private void back() {if(tier>1){tier--;if(tier==1)selectedCategory=-1;if(tier==2)selectedSubcategory=-1;showPanel();}}
    @Override public void onConfigurationChanged(android.content.res.Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (bubble != null && bubbleParams != null) {
            hideDropTarget();
            bubbleParams.x = Math.max(0,
                    Math.min(bubbleParams.x, screenWidth() - dp(54)));
            bubbleParams.y = Math.max(0,
                    Math.min(bubbleParams.y, screenHeight() - dp(54)));
            wm.updateViewLayout(bubble, bubbleParams);
            if (panel != null) showPanel();
        }
    }

    @Override public void onDestroy() {
        hideDropTarget();
        hidePanel();
        if (bubble != null) {
            wm.removeView(bubble);
            bubble = null;
        }
        super.onDestroy();
    }
}
