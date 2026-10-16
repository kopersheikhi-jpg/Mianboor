package com.mianboor.app

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

/**
 * TextView با متن گرادیانی نئون — برای لوگوی MianBoor
 * "Mian" صورتی‌بنفش، "Boor" آبی (مطابق تصویر مرجع)
 */
class GradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    var startColor: Int = ContextCompat.getColor(context, R.color.neon_pink)
        set(value) {
            field = value
            applyShader()
        }

    var endColor: Int = ContextCompat.getColor(context, R.color.neon_blue)
        set(value) {
            field = value
            applyShader()
        }

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.GradientTextView)
            startColor = a.getColor(R.styleable.GradientTextView_gtStartColor, startColor)
            endColor = a.getColor(R.styleable.GradientTextView_gtEndColor, endColor)
            a.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyShader()
    }

    private fun applyShader() {
        if (width > 0 && height > 0) {
            paint.shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                startColor, endColor, Shader.TileMode.CLAMP
            )
        }
    }
}
