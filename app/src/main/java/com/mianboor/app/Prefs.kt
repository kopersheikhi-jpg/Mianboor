package com.mianboor.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("mianboor_prefs", Context.MODE_PRIVATE)

    // ---------- اشتراک ----------
    fun saveSubscription(sub: Subscription) {
        sp.edit()
            .putString("code", sub.code)
            .putLong("expires", sub.expiresAt)
            .putBoolean("trial", sub.isTrial)
            .putString("plan", sub.planName)
            .apply()
    }

    fun loadSubscription(): Subscription? {
        val code = sp.getString("code", null) ?: return null
        val expires = sp.getLong("expires", 0L)
        if (expires <= 0L) return null
        return Subscription(
            code = code,
            expiresAt = expires,
            isTrial = sp.getBoolean("trial", false),
            planName = sp.getString("plan", "") ?: ""
        )
    }

    fun clear() = sp.edit().clear().apply()

    // ---------- تست رایگان ----------
    fun trialUsed(): Boolean = sp.getBoolean("trial_used", false)

    fun markTrialUsed() = sp.edit().putBoolean("trial_used", true).apply()

    // ---------- محافظت در برابر حدس کد ----------
    fun inCooldown(): Boolean = System.currentTimeMillis() < sp.getLong("cooldown_until", 0L)

    fun registerFailure(): Int {
        val fails = sp.getInt("fails", 0) + 1
        if (fails >= 5) {
            sp.edit()
                .putLong("cooldown_until", System.currentTimeMillis() + 60_000L)
                .putInt("fails", 0)
                .apply()
        } else {
            sp.edit().putInt("fails", fails).apply()
        }
        return fails
    }

    fun resetFailures() = sp.edit().putInt("fails", 0).apply()

    // ---------- کش کانفیگ‌ها برای حالت آفلاین ----------
    fun cacheConfigs(list: List<ConfigItem>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("name", it.name).put("uri", it.uri)) }
        sp.edit().putString("configs_cache", arr.toString()).apply()
    }

    fun cachedConfigs(): List<ConfigItem> = try {
        val arr = JSONArray(sp.getString("configs_cache", "[]"))
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            ConfigItem(o.getString("name"), o.getString("uri"))
        }
    } catch (e: Exception) {
        emptyList()
    }
}
