# J2ME-Loader Floating Bubble Remake Pack

Semua file siap pakai untuk fitur **popup bubble** ala Facebook Messenger
di dalam remake J2ME-Loader.

## Isi folder

```
j2me-loader-bubble-remake/
├── README.md                          ← file ini
├── docs/
│   ├── INTEGRATION.md                 ← panduan pasang lengkap
│   └── AndroidManifest_SNIPPET.xml    ← permission + service
├── java/ru/playsoftware/j2meloader/bubble/
│   ├── FloatingBubbleService.java     ← Chat Heads klasik (utama)
│   ├── BubblePermissionHelper.java    ← request "Draw over other apps"
│   └── ModernBubbleHelper.java        ← Android 11+ Notification Bubbles
└── res/
    ├── layout/
    │   ├── bubble_head.xml
    │   ├── bubble_panel.xml
    │   └── bubble_close_zone.xml
    └── drawable/
        ├── bubble_circle_bg.xml
        ├── bubble_badge_bg.xml
        ├── bubble_panel_bg.xml
        ├── bubble_btn_bg.xml
        ├── bubble_btn_danger.xml
        └── bubble_close_circle.xml
```

## Fitur
- Bubble bisa digeser, snap ke tepi
- Tap → mini control panel (Pause, Soft L/R, Exit Game, Hide)
- Drag ke zona X bawah → close
- Foreground service + notifikasi
- Permission helper
- Versi modern (Notification Bubbles) sebagai alternatif

## Cara pakai cepat
1. Baca `docs/INTEGRATION.md`
2. Copy semua file ke project J2ME-Loader sesuai path
3. Update Manifest
4. Panggil:
   ```java
   if (BubblePermissionHelper.requestIfNeeded(this)) {
       FloatingBubbleService.start(this, "Nama Game");
   }
   ```

Dibuat oleh J2me loader remake mode.
