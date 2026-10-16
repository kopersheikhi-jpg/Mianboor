package com.mianboor.app

import android.util.Base64
import java.security.MessageDigest

object Crypto {

    /** هش SHA-256 (برای اعتبارسنجی کد اشتراک بدون ذخیره‌ی کد خام) */
    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** رمزنگاری XOR + Base64 (برای پنهان‌کردن کانفیگ‌ها از خزنده‌های ساده) */
    fun xorEncode(input: String, key: String = AppConfig.SECRET_XOR_KEY): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val kb = key.toByteArray(Charsets.UTF_8)
        val out = ByteArray(bytes.size) { i ->
            (bytes[i].toInt() xor kb[i % kb.size].toInt()).toByte()
        }
        return Base64.encodeToString(out, Base64.NO_WRAP)
    }

    fun xorDecode(encoded: String, key: String = AppConfig.SECRET_XOR_KEY): String? = try {
        val bytes = Base64.decode(encoded.trim(), Base64.NO_WRAP)
        val kb = key.toByteArray(Charsets.UTF_8)
        val out = ByteArray(bytes.size) { i ->
            (bytes[i].toInt() xor kb[i % kb.size].toInt()).toByte()
        }
        String(out, Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }

    private val plainPrefixes = listOf(
        "vless://", "vmess://", "trojan://", "ss://",
        "hysteria2://", "hy2://", "hysteria://", "tuic://", "wireguard://"
    )

    /** اگر uri خام بود همان را برمی‌گرداند، وگرنه رمزگشایی می‌کند */
    fun decodeUri(raw: String): String {
        val t = raw.trim()
        if (plainPrefixes.any { t.startsWith(it) }) return t
        return xorDecode(t) ?: ""
    }
}
