package com.kieronquinn.app.taptap.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.utils.isAccessibilityServiceEnabled

class SettingsFragment : BaseSettingsFragment() {

    private var shouldShowSystemApps: Boolean = true

    private val returnReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            context?.unregisterReceiver(this)
            startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        addPreferencesFromResource(R.xml.settings_main)
        getPreference("gesture"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsGestureFragment)
                true
            }
        }
        getPreference("gates"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsGateFragment)
                true
            }
        }
        getPreference("feedback"){
            it.setOnPreferenceClickListener {
                navigate(R.id.action_settingsFragment_to_settingsFeedbackFragment)
                true
            }
        }
//        getPreference("battery_optimisation"){
//            it.setOnPreferenceClickListener {
//                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
//                    data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
//                }
//                startActivity(intent)
//                true
//            }
//        }
//        getPreference("about_battery"){
//            it.setOnPreferenceClickListener {
//                showBatteryInfoBottomSheet()
//                true
//            }
//        }
//
//        findPreference<Preference>("about_about")?.apply {
//            title = getString(R.string.about, getString(R.string.app_name), BuildConfig.VERSION_NAME)
//            summary = getString(R.string.about_summary, getString(R.string.about_summary_contributors))
//        }
//        findPreference<Preference>("about_libraries")?.apply {
//            setOnPreferenceClickListener {
//                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
//                OssLicensesMenuActivity.setActivityTitle(getString(R.string.libraries))
//                true
//            }
//        }
//        context?.let { context ->
//            Links.setupPreference(context, preferenceScreen, "about_github", Links.LINK_GITHUB)
//            Links.setupPreference(context, preferenceScreen, "about_xda", Links.LINK_XDA)
//            Links.setupPreference(context, preferenceScreen, "about_donate", Links.LINK_DONATE)
//            Links.setupPreference(context, preferenceScreen, "about_twitter", Links.LINK_TWITTER)
//        }
        setHasOptionsMenu(true)
    }



    override fun onResume() {
        super.onResume()
        setHomeAsUpEnabled(false)
        val isServiceEnabled = isAccessibilityServiceEnabled(requireContext(), TapAccessibilityService::class.java)
        getPreference("accessibility"){
            if(isServiceEnabled){
                it.title = getString(R.string.accessibility_info_on)
                it.summary = getString(R.string.accessibility_info_on_desc)
                it.icon = ContextCompat.getDrawable(it.context, R.drawable.ic_accessibility_check_round)
            }else{
                it.title = getString(R.string.accessibility_info_off)
                it.summary = getString(R.string.accessibility_info_off_desc)
                it.icon = ContextCompat.getDrawable(it.context, R.drawable.ic_accessibility_cross_round)
            }
            it.setOnPreferenceClickListener { _ ->
                context?.registerReceiver(returnReceiver, IntentFilter(TapAccessibilityService.KEY_ACCESSIBILITY_START))
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                Toast.makeText(it.context, R.string.accessibility_info_toast, Toast.LENGTH_LONG).show()
                true
            }
        }
//        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
//        getPreference("battery_optimisation"){
//            it.isVisible = !powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
//        }
//
        view?.post {
            getPreference("accessibility"){
                if(isServiceEnabled) {
                    it.setBackgroundTint(null)
                }else{
                    it.setBackgroundTint(ContextCompat.getColor(it.context, R.color.accessibility_cross_circle))
                }
            }
//            getPreference("battery_optimisation"){
//                it.setBackgroundTint(ContextCompat.getColor(it.context, R.color.icon_circle_10))
//            }
        }
    }

//    private fun calibrationRoom(shouldShow: Boolean, callback: (() -> Unit)) {
////        val intent = Intent()
////        //获取intent对象
////        intent.setClass(this, airConditoner::class.java)
////        // 获取class是使用::反射
////        startActivity(intent)
//        navigate(R.id.action_settingsFragment_to_settingsFeedbackFragment)
//
////        swipeRefreshLayout.isRefreshing = true
////        val adapter = recyclerView.adapter as AppsAdapter
////        getApps(shouldShow, searchTerm) {
////            adapter.apps = it
////            adapter.notifyDataSetChanged()
////            shouldShowSystemApps = shouldShow
////            swipeRefreshLayout.isRefreshing = false
////            callback.invoke()
////        }
//
//    }
//
//    private fun setupDots() {
//        val dots = activity?.findViewById<ImageView>(R.id.menu)
//        dots?.let {
//            dots.visibility = View.VISIBLE
//            val popupMenu = PopupMenu(it.context, it)
//            popupMenu.gravity = Gravity.END
//            popupMenu.inflate(R.menu.menu_calibration)
//            val checkBox = popupMenu.menu.findItem(R.id.menu_calibration)
//            checkBox.isChecked = shouldShowSystemApps
//            checkBox.setOnMenuItemClickListener {
//                checkBox.isEnabled = false
//                checkBox.isChecked = !checkBox.isChecked
//                calibrationRoom(checkBox.isChecked) {
//                    checkBox.isEnabled = true
//                }
//                true
//            }
//            dots.setOnClickListener {
//                popupMenu.show()
//            }
//        }
//    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_calibration, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.SettingsCalibrationFragment -> {
                navigate(R.id.action_settingsFragment_to_settingsCalibrationFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.menu_alpha -> {
//                GenericBottomSheetFragment.create(getString(R.string.bs_alpha), R.string.bs_alpha_title, android.R.string.ok).show(childFragmentManager, "bs_alpha")
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return NavigationUI.
//        onNavDestinationSelected(item,requireView().findNavController())
//                || super.onOptionsItemSelected(item)
//    }
//    private fun showBatteryInfoBottomSheet(){
//        GenericBottomSheetFragment.create(getString(R.string.bs_battery_content), R.string.battery_and_optimisation, android.R.string.ok, R.string.bs_battery_positive, "https://dontkillmyapp.com/").show(childFragmentManager, "bs_alpha")
//    }

}