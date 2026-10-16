package com.mianboor.app

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** ابزارهای نمایش اعداد و تاریخ فارسی */
object Fa {

    private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun num(n: Int): String = num(n.toString())

    fun num(raw: String): String {
        val sb = StringBuilder()
        for (c in raw) sb.append(if (c in '0'..'9') faDigits[c - '0'] else c)
        return sb.toString()
    }

    /** تاریخ شمسی (جلالی) از میلی‌ثانیه — فرمت ۱۴۰۴/۰۸/۰۱ */
    fun date(millis: Long): String {
        return try {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"), Locale.US)
            cal.timeInMillis = millis
            val gy = cal.get(Calendar.YEAR)
            val gm = cal.get(Calendar.MONTH) + 1
            val gd = cal.get(Calendar.DAY_OF_MONTH)
            val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
            num("%04d/%02d/%02d".format(Locale.US, jy, jm, jd))
        } catch (e: Exception) {
            try {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"), Locale.US)
                cal.timeInMillis = millis
                "%04d/%02d/%02d".format(
                    Locale.US,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            } catch (e2: Exception) {
                ""
            }
        }
    }

    private val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    /** الگوریتم استاندارد تبدیل میلادی به شمسی (بدون وابستگی خارجی) */
    private fun gregorianToJalali(gy0: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        var gy = gy0
        val jyBase = if (gy <= 1600) 0 else 979
        gy -= if (gy <= 1600) 621 else 1600
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 +
                gd + gDaysInMonth[gm - 1]
        var jy = jyBase + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + (if (days < 186) days % 31 else (days - 186) % 30)
        return Triple(jy, jm, jd)
    }

    /** نمایش کد به‌صورت ماسک‌شده: ۱۲۳••۸۹ */
    fun maskCode(code: String): String = when {
        code == "TRIAL" -> "—"
        code.length >= 6 -> num(code.take(3)) + "••" + num(code.takeLast(2))
        else -> num(code)
    }
}
