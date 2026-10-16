package com.mianboor.app

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.mianboor.app.databinding.DialogGateBinding
import kotlinx.coroutines.launch

/**
 * پاپ‌آپ اصلی اپ — همان پنجره‌ای که به محض باز شدن اپ نمایش داده می‌شود:
 *  • ورود کد اشتراک عددی
 *  • خرید اشتراک
 *  • تست رایگان
 *  • پشتیبانی
 */
class GateDialogFragment : DialogFragment() {

    private var _b: DialogGateBinding? = null
    private val b get() = _b!!
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Mianboor_GateDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return d
    }

    override fun onStart() {
        super.onStart()
        val win = dialog?.window ?: return
        win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dm = resources.displayMetrics
        val marginPx = (24 * dm.density).toInt()
        win.setLayout(dm.widthPixels - marginPx, ViewGroup.LayoutParams.WRAP_CONTENT)
        win.setWindowAnimations(R.style.DialogAnim)
        win.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogGateBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        b.etCode.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(AppConfig.CODE_MAX_LEN))
        b.etCode.doAfterTextChanged { b.tvError.visibility = View.GONE }

        b.btnActivate.setOnClickListener { activateCode() }
        b.rowBuy.setOnClickListener { open(BuyActivity::class.java) }
        b.rowTrial.setOnClickListener { startTrial() }
        b.rowSupport.setOnClickListener { open(SupportActivity::class.java) }
    }

    /** اعتبارسنجی کد اشتراک عددی با پنل تاییدشده */
    private fun activateCode() {
        val code = b.etCode.text?.toString()?.trim().orEmpty()
        b.tvError.visibility = View.GONE

        if (code.length < AppConfig.CODE_MIN_LEN) {
            showError(getString(R.string.err_invalid_code))
            return
        }
        if (prefs.inCooldown()) {
            showError(getString(R.string.err_cooldown))
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val res = RemoteRepo.fetchApprovedCodes()) {
                is PanelResult.Error -> {
                    setLoading(false)
                    showError(res.message)
                }
                is PanelResult.Success -> {
                    val hash = Crypto.sha256Hex(code)
                    val match = res.codes.firstOrNull {
                        it.active && it.hash.equals(hash, ignoreCase = true) && it.days >= AppConfig.MIN_DAYS
                    }
                    if (match == null) {
                        prefs.registerFailure()
                        setLoading(false)
                        showError(getString(R.string.err_invalid_code))
                    } else {
                        prefs.resetFailures()
                        prefs.saveSubscription(
                            Subscription(
                                code = code,
                                expiresAt = System.currentTimeMillis() + match.days * 86_400_000L,
                                isTrial = false,
                                planName = match.plan
                            )
                        )
                        Toast.makeText(requireContext(), R.string.activated_ok, Toast.LENGTH_SHORT).show()
                        goDashboard()
                    }
                }
            }
        }
    }

    /** تست رایگان — یک‌بار برای هر دستگاه */
    private fun startTrial() {
        b.tvError.visibility = View.GONE
        if (prefs.trialUsed()) {
            showError(getString(R.string.trial_used))
            return
        }
        prefs.markTrialUsed()
        prefs.saveSubscription(
            Subscription(
                code = "TRIAL",
                expiresAt = System.currentTimeMillis() + AppConfig.TRIAL_DAYS * 86_400_000L,
                isTrial = true,
                planName = getString(R.string.trial_plan)
            )
        )
        Toast.makeText(requireContext(), R.string.trial_ok, Toast.LENGTH_LONG).show()
        goDashboard()
    }

    private fun goDashboard() {
        dismiss()
        startActivity(Intent(requireContext(), DashboardActivity::class.java))
        activity?.finish()
    }

    private fun open(cls: Class<*>) {
        startActivity(Intent(requireContext(), cls))
    }

    private fun setLoading(on: Boolean) {
        b.progress.visibility = if (on) View.VISIBLE else View.GONE
        b.btnActivate.isEnabled = !on
        b.rowBuy.isEnabled = !on
        b.rowTrial.isEnabled = !on
        b.rowSupport.isEnabled = !on
        if (on) b.tvError.visibility = View.GONE
    }

    private fun showError(msg: String) {
        b.tvError.text = msg
        b.tvError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
