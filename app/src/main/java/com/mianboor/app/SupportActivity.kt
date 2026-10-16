package com.mianboor.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mianboor.app.databinding.ActivitySupportBinding

/** صفحه پشتیبانی — تلگرام و ایمیل */
class SupportActivity : AppCompatActivity() {

    private lateinit var b: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnBack.setOnClickListener { finish() }
        b.cardTelegram.setOnClickListener { openUrl(AppConfig.TELEGRAM_SUPPORT) }
        b.cardEmail.setOnClickListener { sendEmail() }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.err_open_link, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${AppConfig.SUPPORT_EMAIL}")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.err_open_link, Toast.LENGTH_SHORT).show()
        }
    }
}
