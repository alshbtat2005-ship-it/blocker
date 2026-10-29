package com.mohammad.touchblocker

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class BlockerService : AccessibilityService() {

    private lateinit var wm: WindowManager
    private val views = mutableListOf<View>()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }

    override fun onServiceConnected() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        ZonePrefs.prefs(this).registerOnSharedPreferenceChangeListener(listener)
        refresh()
    }

    private fun screenSize(): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= 30) {
            val b = wm.maximumWindowMetrics.bounds
            Pair(b.width(), b.height())
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(dm)
            Pair(dm.widthPixels, dm.heightPixels)
        }
    }

    private fun clear() {
        views.forEach { try { wm.removeView(it) } catch (_: Exception) {} }
        views.clear()
    }

    private fun refresh() {
        clear()
        val (sw, sh) = screenSize()
        val showRed = ZonePrefs.showRed(this)
        for (z in ZonePrefs.load(this)) {
            val w = ((z.right - z.left) * sw).toInt().coerceAtLeast(1)
            val h = ((z.bottom - z.top) * sh).toInt().coerceAtLeast(1)
            val v = View(this).apply {
                setBackgroundColor(if (showRed) Color.argb(70, 255, 0, 0) else Color.TRANSPARENT)
                setOnTouchListener { _, _ -> true } // يبلع اللمس
            }
            val lp = WindowManager.LayoutParams(
                w, h,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = (z.left * sw).toInt()
                y = (z.top * sh).toInt()
            }
            try {
                wm.addView(v, lp)
                views.add(v)
            } catch (_: Exception) {}
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::wm.isInitialized) refresh()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (::wm.isInitialized) {
            ZonePrefs.prefs(this).unregisterOnSharedPreferenceChangeListener(listener)
            clear()
        }
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
