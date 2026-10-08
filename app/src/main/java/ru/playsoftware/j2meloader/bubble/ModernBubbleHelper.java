package ru.playsoftware.j2meloader.bubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import ru.playsoftware.j2meloader.MainActivity;
import ru.playsoftware.j2meloader.R;

/**
 * Versi modern: Android 11+ Notification Bubbles API
 * (mirip Messenger terbaru).
 *
 * Gunakan ini sebagai alternatif / tambahan selain FloatingBubbleService.
 * Butuh targetSdk 30+ dan Bubble metadata.
 *
 * Catatan: Bubble API lebih "system controlled".
 * Untuk full custom control game, FloatingBubbleService (overlay)
 * lebih powerful. Bubble bagus untuk quick-open activity.
 */
public class ModernBubbleHelper {

    private static final String CHANNEL_ID = "j2me_modern_bubble";
    private static final int BUBBLE_NOTIF_ID = 3030;

    public static void showBubble(Context context, String title, String body) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Fallback ke classic bubble
            FloatingBubbleService.start(context, title);
            return;
        }

        createChannel(context);

        Intent target = new Intent(context, MainActivity.class);
        target.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent bubbleIntent = PendingIntent.getActivity(
                context, 0, target,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        // Bubble metadata
        NotificationCompat.BubbleMetadata bubbleData =
                new NotificationCompat.BubbleMetadata.Builder(bubbleIntent,
                        IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                        .setDesiredHeight(600)
                        .setAutoExpandBubble(false)
                        .setSuppressNotification(false)
                        .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setBubbleMetadata(bubbleData)
                .setContentIntent(bubbleIntent)
                .setAutoCancel(false);

        NotificationManagerCompat.from(context).notify(BUBBLE_NOTIF_ID, builder.build());
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "J2ME Modern Bubbles",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Android 11+ Bubbles for J2ME Loader");
            channel.setAllowBubbles(true);
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public static void cancel(Context context) {
        NotificationManagerCompat.from(context).cancel(BUBBLE_NOTIF_ID);
    }
}
