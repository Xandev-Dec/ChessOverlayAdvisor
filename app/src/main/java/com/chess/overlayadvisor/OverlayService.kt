package com.chess.overlayadvisor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var panel: LinearLayout? = null
    private var guide: BoardGuideView? = null

    override fun onCreate() {
        super.onCreate()
        startNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (panel == null) {
            try {
                showOverlay()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Gagal menampilkan panel: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startNotification() {
        val channelId = "chess_overlay_channel"

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Chess Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Chess Overlay aktif")
            .setContentText("Panel melayang sedang berjalan")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(1, notification)
        }
    }

    private fun showOverlay() {
        val metrics = resources.displayMetrics
        val density = metrics.density

        // Grid 8x8: persegi selebar layar (papan di aplikasi catur umumnya penuh lebar),
        // di tengah vertikal. Posisi dan ukuran bisa disesuaikan dengan tombol.
        val boardSize = metrics.widthPixels.toFloat()
        val left = 0f
        val top = (metrics.heightPixels - boardSize) / 2f

        val guideView = BoardGuideView(this).apply {
            boardRect.set(left, top, left + boardSize, top + boardSize)
            visibility = View.GONE
        }
        guide = guideView

        // Lapisan grid tidak bisa disentuh, jadi tap tetap sampai ke aplikasi catur.
        val guideParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        windowManager.addView(guideView, guideParams)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            background = GradientDrawable().apply {
                setColor(Color.rgb(25, 32, 45))
                cornerRadius = 20f
                setStroke(2, Color.rgb(80, 150, 255))
            }
        }

        val header = TextView(this).apply {
            text = "♟ CHESS OVERLAY"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 12)
        }

        val sizeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val moveRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val resizeStep = 12f * density
        val moveStep = 10f * density

        sizeRow.addView(makeButton("GRID") {
            guideView.visibility =
                if (guideView.visibility == View.VISIBLE) View.GONE
                else View.VISIBLE
        })
        sizeRow.addView(makeButton("+") { resizeGuide(resizeStep) })
        sizeRow.addView(makeButton("-") { resizeGuide(-resizeStep) })
        sizeRow.addView(makeButton("X") { stopSelf() })

        moveRow.addView(makeButton("←") { moveGuide(-moveStep, 0f) })
        moveRow.addView(makeButton("↑") { moveGuide(0f, -moveStep) })
        moveRow.addView(makeButton("↓") { moveGuide(0f, moveStep) })
        moveRow.addView(makeButton("→") { moveGuide(moveStep, 0f) })

        root.addView(header)
        root.addView(sizeRow)
        root.addView(moveRow)
        panel = root

        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 180
        }
        windowManager.addView(root, panelParams)
    }

    /** step > 0 membesarkan grid, step < 0 mengecilkan (dalam piksel per sisi). */
    private fun resizeGuide(step: Float) {
        val view = guide ?: return
        val metrics = resources.displayMetrics
        val rect = view.boardRect
        val delta: Float
        if (step > 0f) {
            // Membesar: berhenti tepat di tepi layar, bukan menolak langkah penuh.
            delta = minOf(
                step,
                rect.left,
                metrics.widthPixels - rect.right,
                rect.top,
                metrics.heightPixels - rect.bottom
            )
        } else {
            // Mengecil: jaga ukuran minimum.
            val minWidth = 160f * metrics.density
            if (rect.width() <= minWidth) return
            delta = maxOf(step, -(rect.width() - minWidth) / 2f)
        }
        if (delta == 0f) return
        rect.inset(-delta, -delta)
        view.invalidate()
    }

    /** Geser grid (dalam piksel); posisinya dijaga tetap di dalam layar. */
    private fun moveGuide(dx: Float, dy: Float) {
        val view = guide ?: return
        val metrics = resources.displayMetrics
        val rect = view.boardRect
        val minDx = -rect.left
        val maxDx = metrics.widthPixels - rect.right
        val minDy = -rect.top
        val maxDy = metrics.heightPixels - rect.bottom
        val moveX = if (minDx <= maxDx) dx.coerceIn(minDx, maxDx) else 0f
        val moveY = if (minDy <= maxDy) dy.coerceIn(minDy, maxDy) else 0f
        if (moveX == 0f && moveY == 0f) return
        rect.offset(moveX, moveY)
        view.invalidate()
    }

    private fun makeButton(
        label: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 11f
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.rgb(45, 105, 190))
            setOnClickListener { action() }
        }
    }

    override fun onDestroy() {
        panel?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        guide?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        panel = null
        guide = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
