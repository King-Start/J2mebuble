# J2ME-Loader Floating Bubble Remake
## Cara Pasang (semua file sudah disiapkan)

### 1. Copy file Java
```
app/src/main/java/ru/playsoftware/j2meloader/bubble/
    ├── FloatingBubbleService.java
    └── BubblePermissionHelper.java
```

### 2. Copy layout
```
app/src/main/res/layout/
    ├── bubble_head.xml
    ├── bubble_panel.xml
    └── bubble_close_zone.xml
```

### 3. Copy drawable
```
app/src/main/res/drawable/
    ├── bubble_circle_bg.xml
    ├── bubble_badge_bg.xml
    ├── bubble_panel_bg.xml
    ├── bubble_btn_bg.xml
    ├── bubble_btn_danger.xml
    └── bubble_close_circle.xml
```

### 4. Update AndroidManifest.xml
Lihat file `AndroidManifest_SNIPPET.xml`

### 5. Cara start bubble (dari Activity / MicroActivity)

```java
// Cek permission dulu
if (BubblePermissionHelper.requestIfNeeded(this)) {
    FloatingBubbleService.start(this, "Game Name");
}

// Stop
FloatingBubbleService.stop(this);
```

### 6. onActivityResult (opsional, untuk handle kembali dari Settings)
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (BubblePermissionHelper.onActivityResult(this, requestCode)) {
        FloatingBubbleService.start(this, "J2ME");
    }
}
```

### 7. Broadcast receiver (opsional – sambungkan ke emulator)
Di MicroActivity atau tempat handle input:
```java
// Terima aksi dari bubble panel
registerReceiver(new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("ru.playsoftware.j2meloader.ACTION_PAUSE".equals(action)) {
            // pause midlet
        } else if ("ru.playsoftware.j2meloader.ACTION_SOFT1".equals(action)) {
            // softkey left
        } else if ("ru.playsoftware.j2meloader.ACTION_SOFT2".equals(action)) {
            // softkey right
        } else if ("ru.playsoftware.j2meloader.ACTION_EXIT_MIDLET".equals(action)) {
            // exit midlet
        }
    }
}, new IntentFilter() {{
    addAction("ru.playsoftware.j2meloader.ACTION_PAUSE");
    addAction("ru.playsoftware.j2meloader.ACTION_SOFT1");
    addAction("ru.playsoftware.j2meloader.ACTION_SOFT2");
    addAction("ru.playsoftware.j2meloader.ACTION_EXIT_MIDLET");
}});
```

### Fitur yang sudah include
- Draggable bubble (snap ke tepi kiri/kanan)
- Tap → buka mini control panel (Pause, Soft L/R, Exit, Hide)
- Drag ke bawah tengah (zona X merah) → close bubble
- Foreground service + notifikasi
- Permission helper
- Support Android 6+ (TYPE_APPLICATION_OVERLAY)

### Catatan modern (Android 11+)
Versi Notification Bubbles resmi Google bisa ditambahkan nanti
sebagai opsi tambahan (lebih “system-like”, mirip Messenger baru).
Saat ini yang dibuat adalah Chat Heads klasik (lebih fleksibel
untuk control game).
