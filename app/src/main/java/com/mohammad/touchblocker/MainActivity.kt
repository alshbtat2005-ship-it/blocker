package com.mohammad.touchblocker

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var editor: ZoneEditorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(36), dp(16), dp(16))
            setBackgroundColor(Color.parseColor("#F1F5F9"))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        root.addView(TextView(this).apply {
            text = "حدد المناطق الممنوع اللمس فيها"
            textSize = 18f; setTextColor(Color.BLACK)
        })
        root.addView(TextView(this).apply {
            text = "اسحب المستطيل لتحريكه، واسحب الدائرة البيضاء في زاويته لتغيير الحجم. الحفظ تلقائي."
            textSize = 13f; setTextColor(Color.DKGRAY)
        })

        editor = ZoneEditorView(this)
        editor.zones.addAll(ZonePrefs.load(this))
        editor.onChanged = { ZonePrefs.save(this, editor.zones) }
        root.addView(editor, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1f
        ).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(8); bottomMargin = dp(8) })

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(Button(this).apply {
            text = "+ إضافة منطقة"; setOnClickListener { editor.addZone() }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(Button(this).apply {
            text = "حذف المحددة"; setOnClickListener { editor.deleteSelected() }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(row)

        root.addView(CheckBox(this).apply {
            text = "إظهار المناطق على الشاشة بلون أحمر خفيف (للتجربة)"
            isChecked = ZonePrefs.showRed(this@MainActivity)
            setOnCheckedChangeListener { _, v -> ZonePrefs.setShowRed(this@MainActivity, v) }
        })

        root.addView(Button(this).apply {
            text = "تفعيل الخدمة من إعدادات إمكانية الوصول"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        setContentView(root)
    }
}
