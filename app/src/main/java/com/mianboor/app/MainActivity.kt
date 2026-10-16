package com.mianboor.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mianboor.app.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * صفحه شروع: اسپلش نئون → بررسی اشتراک → پاپ‌آپ گیت (خرید / تست رایگان / پشتیبانی / کد اشتراک)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        // ورود از داشبورد به‌خاطر انقضای اشتراک
        if (intent.getBooleanExtra("expired", false)) {
            prefs.clear()
            Toast.makeText(this, R.string.expired_toast, Toast.LENGTH_LONG).show()
        }

        // انیمیشن ورود اسپلش
        b.splashContent.alpha = 0f
        b.splashContent.animate().alpha(1f).setDuration(600).start()

        lifecycleScope.launch {
            delay(1500)
            route()
        }

        b.btnUnlock.setOnClickListener {
            showGate()
        }
        b.btnExit.setOnClickListener {
            finishAffinity()
        }
    }

    private fun route() {
        val sub = prefs.loadSubscription()
        if (sub != null && !sub.isExpired()) {
            // اشتراک فعال → مستقیم به داشبورد
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            if (sub != null) prefs.clear()
            showLocked()
            showGate()
        }
    }

    private fun showLocked() {
        b.splashContent.visibility = View.GONE
        b.lockedView.visibility = View.VISIBLE
    }

    private fun showGate() {
        if (supportFragmentManager.findFragmentByTag("gate")?.isAdded == true) return
        GateDialogFragment().show(supportFragmentManager, "gate")
    }
}
