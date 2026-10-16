package com.mianboor.app

import android.icu.util.PersianCalendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** ابزارهای نمایش اعداد و تاریخ فارسی */
object Fa {

    private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun num(n: Int): String = num(n.toString())

    fun num(raw: String): String {
        val sb = StringBuilder()
        for (c in raw) sb.append(if (c in '0'..'9') faDigits[c - '0'] else c)
        return sb.toString()
    }

    /** تاریخ شمسی از میلی‌ثانیه */
    fun date(millis: Long): String = try {
        val cal = PersianCalendar()
        cal.timeInMillis = millis
        val fmt = android.icu.text.SimpleDateFormat("yyyy/MM/dd", Locale("fa"))
        fmt.format(cal)
    } catch (e: Exception) {
        try {
            SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(millis))
        } catch (e2: Exception) {
            ""
        }
    }

    /** نمایش کد به‌صورت ماسک‌شده: 123••••89 */
    fun maskCode(code: String): String = when {
        code == "TRIAL" -> "—"
        code.length >= 6 -> num(code.take(3)) + "••" + num(code.takeLast(2))
        else -> num(code)
    }
}
