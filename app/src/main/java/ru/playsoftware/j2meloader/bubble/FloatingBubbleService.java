package ru.playsoftware.j2meloader.bubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import ru.playsoftware.j2meloader.R;

/**
 * Floating Bubble Service (Messenger-style Chat Heads)
 * for J2ME-Loader remake.
 *
 * Features:
 * - Draggable bubble
 * - Tap to expand mini control panel
 * - Long press + drag to close zone
 * - Foreground service so it stays alive
 */
public class FloatingBubbleService extends Service {

    public static final String ACTION_SHOW = "ru.playsoftware.j2meloader.bubble.SHOW";
    public static final String ACTION_HIDE = "ru.playsoftware.j2meloader.bubble.HIDE";
    public static final String EXTRA_TITLE = "title";

    private static final String CHANNEL_ID = "j2me_bubble_channel";
    private static final int NOTIF_ID = 2026;

    private WindowManager windowManager;
    private View bubbleView;
    private View panelView;
    private View closeZoneView;

    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;
    private WindowManager.LayoutParams closeParams;

    private boolean panelExpanded = false;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private int screenWidth, screenHeight;

    private String bubbleTitle = "J2ME";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification("Bubble aktif"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_HIDE.equals(action)) {
                stopSelf();
                return START_NOT_STICKY;
            }
            if (intent.hasExtra(EXTRA_TITLE)) {
                bubbleTitle = intent.getStringExtra(EXTRA_TITLE);
            }
        }

        if (bubbleView == null) {
            showBubble();
        }
        return START_STICKY;
    }

    private void showBubble() {
        // ===== BUBBLE =====
        bubbleView = LayoutInflater.from(this).inflate(R.layout.bubble_head, null);
        ImageView icon = bubbleView.findViewById(R.id.bubble_icon);
        TextView badge = bubbleView.findViewById(R.id.bubble_badge);
        badge.setText(bubbleTitle.length() > 4 ? bubbleTitle.substring(0, 4) : bubbleTitle);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        bubbleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = screenWidth - 160;
        bubbleParams.y = screenHeight / 3;

        bubbleView.setOnTouchListener(new BubbleTouchListener());

        windowManager.addView(bubbleView, bubbleParams);

        // ===== CLOSE ZONE (hidden by default) =====
        closeZoneView = LayoutInflater.from(this).inflate(R.layout.bubble_close_zone, null);
        closeParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );
        closeParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        closeParams.y = 80;
        closeZoneView.setVisibility(View.GONE);
        windowManager.addView(closeZoneView, closeParams);

        // ===== MINI PANEL (hidden) =====
        panelView = LayoutInflater.from(this).inflate(R.layout.bubble_panel, null);
        panelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelView.setVisibility(View.GONE);

        // Panel buttons
        panelView.findViewById(R.id.btn_pause).setOnClickListener(v -> {
            sendBroadcast(new Intent("ru.playsoftware.j2meloader.ACTION_PAUSE"));
            Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
            collapsePanel();
        });
        panelView.findViewById(R.id.btn_soft1).setOnClickListener(v -> {
            sendBroadcast(new Intent("ru.playsoftware.j2meloader.ACTION_SOFT1"));
            collapsePanel();
        });
        panelView.findViewById(R.id.btn_soft2).setOnClickListener(v -> {
            sendBroadcast(new Intent("ru.playsoftware.j2meloader.ACTION_SOFT2"));
            collapsePanel();
        });
        panelView.findViewById(R.id.btn_close_game).setOnClickListener(v -> {
            sendBroadcast(new Intent("ru.playsoftware.j2meloader.ACTION_EXIT_MIDLET"));
            stopSelf();
        });
        panelView.findViewById(R.id.btn_close_bubble).setOnClickListener(v -> stopSelf());

        windowManager.addView(panelView, panelParams);
    }

    private void expandPanel() {
        if (panelExpanded) {
            collapsePanel();
            return;
        }
        panelParams.x = bubbleParams.x - 40;
        panelParams.y = bubbleParams.y + 90;
        // Keep panel on screen
        if (panelParams.x < 0) panelParams.x = 20;
        if (panelParams.x + 280 > screenWidth) panelParams.x = screenWidth - 300;
        panelView.setVisibility(View.VISIBLE);
        windowManager.updateViewLayout(panelView, panelParams);
        panelExpanded = true;
    }

    private void collapsePanel() {
        if (panelView != null) {
            panelView.setVisibility(View.GONE);
        }
        panelExpanded = false;
    }

    private class BubbleTouchListener implements View.OnTouchListener {
        private static final int CLICK_THRESHOLD = 10;
        private long touchStartTime;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = bubbleParams.x;
                    initialY = bubbleParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    touchStartTime = System.currentTimeMillis();
                    closeZoneView.setVisibility(View.VISIBLE);
                    collapsePanel();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    bubbleParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                    bubbleParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(bubbleView, bubbleParams);

                    // Highlight close zone if near bottom center
                    if (isNearCloseZone(bubbleParams.x, bubbleParams.y)) {
                        closeZoneView.setAlpha(1f);
                        closeZoneView.setScaleX(1.2f);
                        closeZoneView.setScaleY(1.2f);
                    } else {
                        closeZoneView.setAlpha(0.6f);
                        closeZoneView.setScaleX(1f);
                        closeZoneView.setScaleY(1f);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    closeZoneView.setVisibility(View.GONE);
                    long duration = System.currentTimeMillis() - touchStartTime;
                    float dx = Math.abs(event.getRawX() - initialTouchX);
                    float dy = Math.abs(event.getRawY() - initialTouchY);

                    if (isNearCloseZone(bubbleParams.x, bubbleParams.y)) {
                        stopSelf();
                        return true;
                    }

                    // Snap to edge
                    if (bubbleParams.x + 60 < screenWidth / 2) {
                        bubbleParams.x = 0;
                    } else {
                        bubbleParams.x = screenWidth - bubbleView.getWidth();
                    }
                    windowManager.updateViewLayout(bubbleView, bubbleParams);

                    // Click?
                    if (dx < CLICK_THRESHOLD && dy < CLICK_THRESHOLD && duration < 250) {
                        expandPanel();
                    }
                    return true;
            }
            return false;
        }

        private boolean isNearCloseZone(int x, int y) {
            int centerX = screenWidth / 2;
            return Math.abs(x + 40 - centerX) < 120 && y > screenHeight - 280;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "J2ME Floating Bubble",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Bubble control for J2ME games");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String text) {
        Intent hide = new Intent(this, FloatingBubbleService.class);
        hide.setAction(ACTION_HIDE);
        PendingIntent pi = PendingIntent.getService(this, 0, hide,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("J2ME Loader Bubble")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bubbleView != null) {
            try { windowManager.removeView(bubbleView); } catch (Exception ignored) {}
            bubbleView = null;
        }
        if (panelView != null) {
            try { windowManager.removeView(panelView); } catch (Exception ignored) {}
            panelView = null;
        }
        if (closeZoneView != null) {
            try { windowManager.removeView(closeZoneView); } catch (Exception ignored) {}
            closeZoneView = null;
        }
    }

    // ===== Helper to start from Activity =====
    public static void start(Context context, String title) {
        Intent i = new Intent(context, FloatingBubbleService.class);
        i.setAction(ACTION_SHOW);
        i.putExtra(EXTRA_TITLE, title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }

    public static void stop(Context context) {
        Intent i = new Intent(context, FloatingBubbleService.class);
        i.setAction(ACTION_HIDE);
        context.startService(i);
    }
}
