package com.mianboor.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mianboor.app.databinding.ActivityBuyBinding

/** صفحه خرید اشتراک — پلن‌ها و لینک تلگرام (از AppConfig) */
class BuyActivity : AppCompatActivity() {

    private lateinit var b: ActivityBuyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBuyBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnBack.setOnClickListener { finish() }
        b.btnBuyTelegram.setOnClickListener { openUrl(AppConfig.TELEGRAM_BUY) }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.err_open_link, Toast.LENGTH_SHORT).show()
        }
    }
}
