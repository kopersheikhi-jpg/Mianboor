package com.mianboor.app

/**
 * تنظیمات مرکزی اپلیکیشن Mianboor
 * ⚠️ قبل از انتشار، لینک‌های تلگرام و کلید رمزنگاری را مطابق سلیقه خودتان تغییر دهید.
 */
object AppConfig {

    // آدرس فایل‌های پنل در گیت‌هاب (شاخه main)
    private const val REPO_OWNER = "kopersheikhi-jpg"
    private const val REPO_NAME = "Mianboor"
    private const val BRANCH = "main"

    const val CODES_URL = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/$BRANCH/panel/codes.json"
    const val CONFIGS_URL = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/$BRANCH/panel/configs.json"

    /**
     * کلید رمزنگاری کانفیگ‌ها (XOR + Base64)
     * ⚠️ اگر این کلید را تغییر دادید، مقدار XOR_KEY در فایل panel/tools.py را هم عیناً تغییر دهید.
     */
    const val SECRET_XOR_KEY = "Mianboor@2026#Ne0nGl4ss"

    // لینک‌های ارتباطی (این‌ها را با آیدی واقعی خودتان عوض کنید)
    const val TELEGRAM_SUPPORT = "https://t.me/mianboor_support"
    const val TELEGRAM_BUY = "https://t.me/mianboor_support"
    const val SUPPORT_EMAIL = "support@mianboor.app"

    // مدت تست رایگان (روز)
    const val TRIAL_DAYS = 3

    // محدودیت طول کد اشتراک عددی
    const val CODE_MIN_LEN = 8
    const val CODE_MAX_LEN = 14

    // حداقل روزهای اشتراک معتبر در پنل
    const val MIN_DAYS = 1
}
