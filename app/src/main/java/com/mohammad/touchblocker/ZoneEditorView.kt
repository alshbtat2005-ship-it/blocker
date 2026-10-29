package com.mohammad.touchblocker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

/** شاشة هاتف وهمية بنفس نسبة شاشة جهازك، تُرسم عليها المناطق الممنوعة. */
class ZoneEditorView(ctx: Context) : View(ctx) {

    val zones = mutableListOf<RectF>()   // نسب من 0 إلى 1
    var selected = -1
    var onChanged: (() -> Unit)? = null

    private val d = resources.displayMetrics.density
    private val bg = Paint().apply { color = Color.parseColor("#0F172A") }
    private val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#64748B"); style = Paint.Style.STROKE; strokeWidth = 3 * d
    }
    private val fill = Paint().apply { color = Color.argb(110, 229, 57, 53) }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935"); style = Paint.Style.STROKE; strokeWidth = 2 * d
    }
    private val strokeSel = Paint(stroke).apply { strokeWidth = 4 * d; color = Color.WHITE }
    private val handle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 13 * d; textAlign = Paint.Align.CENTER
    }

    private enum class Mode { NONE, MOVE, RESIZE }
    private var mode = Mode.NONE
    private var lx = 0f
    private var ly = 0f

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val dm = resources.displayMetrics
        val ratio = dm.heightPixels.toFloat() / dm.widthPixels
        var w = MeasureSpec.getSize(wSpec)
        var h = MeasureSpec.getSize(hSpec)
        if (w * ratio > h) w = (h / ratio).toInt() else h = (w * ratio).toInt()
        setMeasuredDimension(w, h)
    }

    private fun px(z: RectF) = RectF(z.left * width, z.top * height, z.right * width, z.bottom * height)

    override fun onDraw(c: Canvas) {
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), frame)
        c.drawCircle(width / 2f, 10 * d, 4 * d, frame) // رمز الكاميرا
        for ((i, z) in zones.withIndex()) {
            val r = px(z)
            c.drawRect(r, fill)
            c.drawRect(r, if (i == selected) strokeSel else stroke)
            c.drawText("ممنوع", r.centerX(), r.centerY() + 5 * d, text)
            c.drawCircle(r.right, r.bottom, 11 * d, handle)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                mode = Mode.NONE; selected = -1
                for (i in zones.indices.reversed()) {
                    val r = px(zones[i])
                    val dx = e.x - r.right; val dy = e.y - r.bottom
                    if (dx * dx + dy * dy < (28 * d) * (28 * d)) { selected = i; mode = Mode.RESIZE; break }
                    if (r.contains(e.x, e.y)) { selected = i; mode = Mode.MOVE; break }
                }
                lx = e.x; ly = e.y
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> if (selected >= 0 && mode != Mode.NONE) {
                var dx = (e.x - lx) / width
                var dy = (e.y - ly) / height
                lx = e.x; ly = e.y
                val r = zones[selected]
                if (mode == Mode.MOVE) {
                    dx = dx.coerceIn(-r.left, 1f - r.right)
                    dy = dy.coerceIn(-r.top, 1f - r.bottom)
                    r.offset(dx, dy)
                } else {
                    r.right = (r.right + dx).coerceIn(r.left + 0.05f, 1f)
                    r.bottom = (r.bottom + dy).coerceIn(r.top + 0.05f, 1f)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mode != Mode.NONE) onChanged?.invoke()
                mode = Mode.NONE
            }
        }
        return true
    }

    fun addZone() {
        zones.add(RectF(0.3f, 0.4f, 0.7f, 0.55f))
        selected = zones.lastIndex
        invalidate(); onChanged?.invoke()
    }

    fun deleteSelected() {
        if (selected in zones.indices) {
            zones.removeAt(selected); selected = -1
            invalidate(); onChanged?.invoke()
        }
    }
}
