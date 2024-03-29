package com.kieronquinn.app.taptap

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.android.internal.logging.MetricsLogger
import com.android.systemui.keyguard.WakefulnessLifecycle
import com.google.android.systemui.columbus.ColumbusContentObserver
import com.google.android.systemui.columbus.ColumbusService
import com.google.android.systemui.columbus.ContentResolverWrapper
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.feedback.FeedbackEffect
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.columbus.actions.*
import com.kieronquinn.app.taptap.columbus.feedback.HapticClickCompat
import com.kieronquinn.app.taptap.columbus.feedback.WakeDevice
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.store.ActionListFile
import com.kieronquinn.app.taptap.smaliint.SmaliCalls
import com.kieronquinn.app.taptap.utils.*

class TapAccessibilityService : AccessibilityService(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "TAS"
        val KEY_ACCESSIBILITY_START = "accessibility_start"
    }

    private var columbusService: ColumbusService? = null
    private var gestureSensorImpl: GestureSensorImpl? = null

    private var currentPackageName: String = "android"

    private var wakefulnessLifecycle: WakefulnessLifecycle? = null

    private val sharedPreferences by lazy {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val context = this
        val activityManagerService = try{
            ActivityManager::class.java.getMethod("getService").invoke(null)
        }catch (e: NoSuchMethodException){
            val activityManagerNative = Class.forName("android.app.ActivityManagerNative")
            activityManagerNative.getMethod("getDefault").invoke(null)
        }
        val gestureConfiguration = createGestureConfiguration(context, activityManagerService)
        this.gestureSensorImpl = GestureSensorImpl(context, gestureConfiguration)
        val powerManagerWrapper = PowerManagerWrapper(context)
        val metricsLogger = MetricsLogger()
        val wakefulnessLifecycle = WakefulnessLifecycle()
        this.wakefulnessLifecycle = wakefulnessLifecycle

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        //Set model from prefs
        SmaliCalls.setTapRtModel(TfModel.valueOf(sharedPreferences.getString(SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL4.name) ?: TfModel.PIXEL4.name).model)

        //Create the service
        this.columbusService = ColumbusService::class.java.constructors.first().newInstance(getColumbusActions(), getColumbusFeedback(), getGates(context), gestureSensorImpl, powerManagerWrapper, metricsLogger) as ColumbusService
        configureTap()

        sendBroadcast(Intent(KEY_ACCESSIBILITY_START).setPackage(packageName))
    }

    private fun getColumbusActions() : List<Action> {
        return ActionListFile.loadFromFile(this).toList().mapNotNull { getActionForEnum(it) }
    }

    private fun getActionForEnum(action: ActionInternal) : Action? {
        return try {
            when (action.action) {
                TapAction.SEND_MESSAGE -> SendMessage(this)
            }
        }catch (e: RuntimeException){
            //Enum not found, probably a downgrade issue
            null
        }
    }

    private fun refreshColumbusActions(){
        columbusService?.setActions(getColumbusActions())
    }

    private fun createGestureConfiguration(context: Context, activityManager: Any): GestureConfiguration {
        val contentResolverWrapper = ContentResolverWrapper(context)
        val factory = ColumbusContentObserver.Factory::class.java.constructors.first().newInstance(contentResolverWrapper, activityManager) as ColumbusContentObserver.Factory
        return GestureConfiguration(context, emptySet(), factory)
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        //Stop the service to prevent listeners still being attached
        columbusService?.stop()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName?.toString() != currentPackageName) {
            if(event.packageName?.toString() == "android") return
            currentPackageName = event.packageName?.toString() ?: "android"
            Log.d(TAG, "package $currentPackageName isCamera ${isPackageCamera(currentPackageName)}")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        refreshColumbusActions()
        if(key == SHARED_PREFERENCES_KEY_MODEL){
            val model = TfModel.valueOf(sharedPreferences.getString(SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL4.name) ?: TfModel.PIXEL4.name)
            gestureSensorImpl?.setTfClassifier(assets, model.model)
        }
        if(SHARED_PREFERENCES_FEEDBACK_KEYS.contains(key)){
            //Refresh feedback options
            refreshColumbusFeedback()
        }
        if(key == SHARED_PREFERENCES_KEY_GATES){
            //Refresh gates
            refreshColumbusGates(this)
        }
        if(key == SHARED_PREFERENCES_KEY_ACTIONS_TIME){
            //Refresh actions
            refreshColumbusActions()
        }
        if(key == SHARED_PREFERENCES_KEY_SENSITIVITY){
            //Reconfigure
            configureTap()
        }

    }

    private fun refreshColumbusFeedback(){
        val feedbackSet = getColumbusFeedback()
        Log.d(TAG, "Setting feedback to ${feedbackSet.joinToString(", ")}")
        columbusService?.setFeedback(feedbackSet)
    }

    private fun getColumbusFeedback(): Set<FeedbackEffect> {
        val isVibrateEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_VIBRATE, true)
        val isWakeEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_KEY_FEEDBACK_WAKE, true)
        val feedbackList = ArrayList<FeedbackEffect>()
        if(isVibrateEnabled) feedbackList.add(HapticClickCompat(this))
        if(isWakeEnabled) feedbackList.add(WakeDevice(this))
        return feedbackList.toSet()
    }

    private fun refreshColumbusGates(context: Context){
        val gatesSet = getGates(context)
        Log.d(TAG, "setting gates to ${gatesSet.joinToString(", ")}")
        columbusService?.setGates(gatesSet)
    }

    fun getCurrentPackageName(): String {
        return currentPackageName
    }

    private fun configureTap(){
        gestureSensorImpl?.getTapRT()?.run {
            val sensitivity = sharedPreferences.getString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05")?.toFloatOrNull() ?: 0.05f
            Log.d("TapRT", "getMinNoiseToTolerate ${positivePeakDetector.getMinNoiseToTolerate()} sensitivity $sensitivity")
            positivePeakDetector.setMinNoiseTolerate(sensitivity)
        }
    }
}