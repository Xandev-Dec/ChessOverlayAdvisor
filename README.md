# Chess Overlay Advisor

Aplikasi Android (Kotlin) yang menampilkan panel catur melayang di atas aplikasi lain.

## Fitur saat ini
- Panel kontrol melayang dengan tombol **GRID** (tampil/sembunyi garis grid 8x8 di layar), **+** / **-** (besar/kecil grid), panah **← ↑ ↓ →** (geser grid agar pas dengan papan) dan **X** (tutup)
- Lapisan grid tidak bisa disentuh, jadi tap tetap diteruskan ke aplikasi catur di bawahnya
- Servis foreground dengan notifikasi
- Belum ada engine atau saran langkah, dan panel kontrol belum bisa digeser

## Cara pakai
1. Pasang APK, buka aplikasi, lalu tekan **MULAI OVERLAY**
2. Saat diminta, aktifkan izin "Tampil di atas aplikasi lain"
3. Tekan **MULAI OVERLAY** lagi

## Build
Build otomatis lewat GitHub Actions (`.github/workflows/build.yml`). APK debug ada di bagian **Artifacts** pada tiap run.

Build lokal: `gradle assembleDebug` (Gradle 8.4, JDK 17).

## Struktur
```
app/
  build.gradle
  src/main/
    AndroidManifest.xml
    java/com/chess/overlayadvisor/
      MainActivity.kt
      OverlayService.kt
      BoardGuideView.kt
```
