package com.mianboor.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class PanelResult {
    data class Success(val codes: List<ApprovedCode>) : PanelResult()
    data class Error(val message: String) : PanelResult()
}

sealed class ConfigResult {
    data class Success(val configs: List<ConfigItem>) : ConfigResult()
    data class Error(val message: String) : ConfigResult()
}

/** دریافت کدهای تاییدشده و کانفیگ‌ها از پنل گیت‌هاب */
object RemoteRepo {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("Cache-Control", "no-cache")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            response.body?.string() ?: throw IOException("Empty response body")
        }
    }

    /** دریافت لیست کدهای تاییدشده (فقط هش‌ها) */
    suspend fun fetchApprovedCodes(): PanelResult = try {
        val codesArray = JSONObject(get(AppConfig.CODES_URL)).optJSONArray("codes") ?: JSONArray()
        val codes = buildList {
            for (i in 0 until codesArray.length()) {
                val o = codesArray.getJSONObject(i)
                add(
                    ApprovedCode(
                        hash = o.optString("hash"),
                        plan = o.optString("plan", "اشتراک"),
                        days = o.optInt("days", 30),
                        active = o.optBoolean("active", true),
                        note = o.optString("note")
                    )
                )
            }
        }
        PanelResult.Success(codes)
    } catch (e: Exception) {
        PanelResult.Error(friendlyMessage(e))
    }

    /** دریافت و رمزگشایی کانفیگ‌ها */
    suspend fun fetchConfigs(): ConfigResult = try {
        val configsArray = JSONObject(get(AppConfig.CONFIGS_URL)).optJSONArray("configs") ?: JSONArray()
        val list = buildList {
            for (i in 0 until configsArray.length()) {
                val o = configsArray.getJSONObject(i)
                val uri = Crypto.decodeUri(o.optString("uri"))
                if (uri.isNotBlank()) {
                    add(ConfigItem(name = o.optString("name", "کانفیگ"), uri = uri))
                }
            }
        }
        ConfigResult.Success(list)
    } catch (e: Exception) {
        ConfigResult.Error(friendlyMessage(e))
    }

    private fun friendlyMessage(e: Exception): String {
        val m = e.message ?: ""
        return if (e is IOException &&
            (m.contains("Unable to resolve", true) || m.contains("timeout", true) ||
                    m.contains("connect", true) || m.contains("HTTP", true))
        ) {
            "اتصال اینترنت برقرار نیست یا سرور پنل در دسترس نیست؛ دوباره تلاش کنید"
        } else {
            "خطا در دریافت اطلاعات؛ لطفاً دوباره تلاش کنید"
        }
    }
}
