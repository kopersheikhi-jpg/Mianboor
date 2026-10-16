package com.mianboor.app

/** یک کد اشتراک تاییدشده در پنل (کد هرگز به‌صورت خام ذخیره نمی‌شود، فقط هش SHA-256) */
data class ApprovedCode(
    val hash: String,
    val plan: String,
    val days: Int,
    val active: Boolean,
    val note: String
)

/** یک کانفیگ قابل استفاده */
data class ConfigItem(
    val name: String,
    val uri: String
) {
    val protocol: String
        get() = when {
            uri.startsWith("vless://") -> "VLESS"
            uri.startsWith("vmess://") -> "VMESS"
            uri.startsWith("trojan://") -> "TROJAN"
            uri.startsWith("ss://") -> "SHADOWSOCKS"
            uri.startsWith("hysteria2://") || uri.startsWith("hy2://") -> "HYSTERIA2"
            uri.startsWith("hysteria://") -> "HYSTERIA"
            uri.startsWith("tuic://") -> "TUIC"
            uri.startsWith("wireguard://") -> "WIREGUARD"
            else -> "CONFIG"
        }
}

/** وضعیت اشتراک کاربر روی همین دستگاه */
data class Subscription(
    val code: String,
    val expiresAt: Long,
    val isTrial: Boolean,
    val planName: String
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = now >= expiresAt

    fun daysLeft(now: Long = System.currentTimeMillis()): Int {
        val ms = expiresAt - now
        if (ms <= 0) return 0
        return ((ms + 86_399_999L) / 86_400_000L).toInt()
    }
}
