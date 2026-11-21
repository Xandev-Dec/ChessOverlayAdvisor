package com.chess.overlayadvisor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/** Menggambar bingkai papan dan garis grid 8x8 di dalam [boardRect]. */
class BoardGuideView(context: Context) : View(context) {

    val boardRect = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cell = boardRect.width() / 8f
        canvas.drawRect(boardRect, paint)
        for (i in 1..7) {
            val x = boardRect.left + i * cell
            val y = boardRect.top + i * cell
            canvas.drawLine(x, boardRect.top, x, boardRect.bottom, paint)
            canvas.drawLine(boardRect.left, y, boardRect.right, y, paint)
        }
    }
}
