package com.chess.overlayadvisor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "♟ Chess Overlay"
            textSize = 27f
        }

        val description = TextView(this).apply {
            text = "Floating Chess Panel\n\nAktifkan izin overlay sebelum memulai."
            textSize = 16f
            setPadding(0, 30, 0, 30)
        }

        val startButton = Button(this).apply {
            text = "MULAI OVERLAY"
            setOnClickListener {
                startOverlay()
            }
        }

        val stopButton = Button(this).apply {
            text = "HENTIKAN OVERLAY"
            setOnClickListener {
                stopService(
                    Intent(this@MainActivity, OverlayService::class.java)
                )
                Toast.makeText(
                    this@MainActivity,
                    "Overlay dihentikan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        layout.addView(title)
        layout.addView(description)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATIONS
            )
        }
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(
                this,
                "Aktifkan izin tampil di atas aplikasi lain",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Notifikasi tidak wajib agar overlay jalan, jadi tidak memblokir.
        requestNotificationPermissionIfNeeded()

        try {
            ContextCompat.startForegroundService(
                this,
                Intent(this, OverlayService::class.java)
            )
            Toast.makeText(
                this,
                "Overlay sedang dijalankan",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Gagal: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private companion object {
        const val REQUEST_NOTIFICATIONS = 100
    }
}
