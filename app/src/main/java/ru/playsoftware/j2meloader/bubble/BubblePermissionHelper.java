package ru.playsoftware.j2meloader.bubble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Helper untuk request permission "Display over other apps"
 * (SYSTEM_ALERT_WINDOW) seperti Messenger.
 */
public class BubblePermissionHelper {

    public static final int REQUEST_OVERLAY = 9090;

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * Cek permission. Kalau belum, buka Settings.
     * @return true jika sudah boleh draw overlay
     */
    public static boolean requestIfNeeded(Activity activity) {
        if (canDrawOverlays(activity)) {
            return true;
        }
        Toast.makeText(activity,
                "Izinkan \"Tampilkan di atas aplikasi lain\" untuk Bubble",
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName())
        );
        activity.startActivityForResult(intent, REQUEST_OVERLAY);
        return false;
    }

    /**
     * Panggil dari onActivityResult
     */
    public static boolean onActivityResult(Activity activity, int requestCode) {
        if (requestCode == REQUEST_OVERLAY) {
            return canDrawOverlays(activity);
        }
        return false;
    }
}
