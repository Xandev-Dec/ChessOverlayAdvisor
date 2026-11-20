
package com.chess.overlayadvisor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var panel: LinearLayout? = null
    private var board: TextView? = null
    private var panelSize = 14f
    private var gridVisible = false

    override fun onCreate() {
        super.onCreate()
        startNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    private fun startNotification() {
        val channelId = "chess_overlay_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chess Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }.setContentTitle("Chess Overlay aktif")
            .setContentText("Panel melayang sedang berjalan")
            .setSmallIcon(android.R.drawable.ic_menu_view)
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

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val gridButton = makeButton("GRID") {
            gridVisible = !gridVisible
            board?.visibility =
                if (gridVisible) android.view.View.VISIBLE
                else android.view.View.GONE
        }

        val plusButton = makeButton("+") {
            panelSize = (panelSize + 2f).coerceAtMost(28f)
            board?.textSize = panelSize
        }

        val minusButton = makeButton("-") {
            panelSize = (panelSize - 2f).coerceAtLeast(10f)
            board?.textSize = panelSize
        }

        val closeButton = makeButton("X") {
            stopSelf()
        }

        buttons.addView(gridButton)
        buttons.addView(plusButton)
        buttons.addView(minusButton)
        buttons.addView(closeButton)

        board = TextView(this).apply {
            text = """
                8  ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜
                7  ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟
                6  · · · · · · · ·
                5  · · · · · · · ·
                4  · · · · · · · ·
                3  · · · · · · · ·
                2  ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙
                1  ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖
                   a b c d e f g h
            """.trimIndent()
            textSize = panelSize
            setTextColor(Color.WHITE)
            setPadding(8, 12, 8, 8)
            visibility = android.view.View.GONE
        }

        root.addView(header)
        root.addView(buttons)
        root.addView(board)

        panel = root

        val params = WindowManager.LayoutParams(
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

        try {
            windowManager.addView(root, params)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Gagal menampilkan panel: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
        }
    }

    private fun makeButton(
        label: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 11f
            setTextColor(Color.WHITE)
            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(45, 105, 190)
                )
            )
            setOnClickListener { action() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        panel?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        panel = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
