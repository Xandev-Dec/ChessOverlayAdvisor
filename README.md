# Chess Overlay Advisor

Aplikasi Android (Kotlin) yang menampilkan panel catur melayang di atas aplikasi lain.

## Fitur saat ini
- Panel overlay dengan tombol **GRID** (tampil/sembunyi papan), **+** / **-** (ukuran) dan **X** (tutup)
- Servis foreground dengan notifikasi
- Papan masih berupa teks statis (posisi awal); belum ada engine atau saran langkah

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
```
