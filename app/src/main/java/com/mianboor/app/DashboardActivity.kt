package com.mianboor.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mianboor.app.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch

/**
 * داشبورد کاربر مشترک: وضعیت اشتراک + لیست کانفیگ‌ها با امکان کپی/اشتراک‌گذاری/اتصال
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var b: ActivityDashboardBinding
    private lateinit var prefs: Prefs
    private val items = mutableListOf<ConfigItem>()
    private lateinit var adapter: ConfigAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        adapter = ConfigAdapter(items, ::onCopy, ::onShare, ::onConnect)
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        b.btnLogout.setOnClickListener { confirmLogout() }
        b.btnSupport.setOnClickListener { startActivity(Intent(this, SupportActivity::class.java)) }
        b.btnRefresh.setOnClickListener { loadConfigs(force = true) }
    }

    override fun onResume() {
        super.onResume()
        val sub = prefs.loadSubscription()
        if (sub == null || sub.isExpired()) {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("expired", sub != null)
            )
            finish()
            return
        }
        renderHeader(sub)
        loadConfigs(force = false)
    }

    private fun renderHeader(sub: Subscription) {
        b.tvPlan.text = sub.planName
        b.tvDays.text = getString(R.string.days_left_fmt, Fa.num(sub.daysLeft()))
        b.tvExpires.text = getString(R.string.expires_fmt, Fa.date(sub.expiresAt))
        b.tvCode.text = if (sub.isTrial) getString(R.string.trial_badge) else Fa.maskCode(sub.code)
    }

    private fun loadConfigs(force: Boolean) {
        val cached = prefs.cachedConfigs()
        if (cached.isNotEmpty()) {
            items.clear()
            items.addAll(cached)
            adapter.notifyDataSetChanged()
            updateEmptyState()
        }
        if (cached.isEmpty() || force) {
            b.progress.visibility = View.VISIBLE
            b.tvEmpty.visibility = View.GONE
        }

        lifecycleScope.launch {
            when (val res = RemoteRepo.fetchConfigs()) {
                is ConfigResult.Success -> {
                    b.tvOffline.visibility = View.GONE
                    if (res.configs.isEmpty()) {
                        if (items.isEmpty()) {
                            b.tvEmpty.text = getString(R.string.empty_configs)
                            updateEmptyState()
                        }
                    } else {
                        prefs.cacheConfigs(res.configs)
                        items.clear()
                        items.addAll(res.configs)
                        adapter.notifyDataSetChanged()
                        updateEmptyState()
                    }
                }
                is ConfigResult.Error -> {
                    if (items.isEmpty()) {
                        b.tvEmpty.text = res.message
                        updateEmptyState()
                    } else {
                        b.tvOffline.visibility = View.VISIBLE
                    }
                }
            }
            b.progress.visibility = View.GONE
        }
    }

    private fun updateEmptyState() {
        b.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    // ---------- اکشن‌های کانفیگ ----------

    private fun onCopy(item: ConfigItem) {
        copyToClipboard(item.uri)
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    private fun onShare(item: ConfigItem) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${item.name}\n${item.uri}")
        }
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }

    private fun onConnect(item: ConfigItem) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.uri))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            copyToClipboard(item.uri)
            Toast.makeText(this, R.string.no_client, Toast.LENGTH_LONG).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(ClipboardManager::class.java)
        cm.setPrimaryClip(ClipData.newPlainText("mianboor_config", text))
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this, R.style.Theme_Mianboor_Alert)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                prefs.clear()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
