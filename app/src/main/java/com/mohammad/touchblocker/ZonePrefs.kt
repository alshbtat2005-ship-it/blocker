package com.mohammad.touchblocker

import android.content.Context
import android.content.SharedPreferences
import android.graphics.RectF

/** تخزين المناطق كنسب (0..1) من الشاشة، في مساحة تعمل حتى قبل فك القفل. */
object ZonePrefs {
    fun prefs(ctx: Context): SharedPreferences =
        ctx.createDeviceProtectedStorageContext()
            .getSharedPreferences("zones", Context.MODE_PRIVATE)

    fun load(ctx: Context): MutableList<RectF> {
        val s = prefs(ctx).getString("z", "") ?: ""
        val out = mutableListOf<RectF>()
        if (s.isBlank()) return out
        for (item in s.split(";")) {
            val p = item.split(",")
            if (p.size == 4) {
                try {
                    out.add(RectF(p[0].toFloat(), p[1].toFloat(), p[2].toFloat(), p[3].toFloat()))
                } catch (_: NumberFormatException) {}
            }
        }
        return out
    }

    fun save(ctx: Context, zones: List<RectF>) {
        val s = zones.joinToString(";") { "${it.left},${it.top},${it.right},${it.bottom}" }
        prefs(ctx).edit().putString("z", s).apply()
    }

    fun showRed(ctx: Context) = prefs(ctx).getBoolean("show", false)
    fun setShowRed(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("show", v).apply()
}
