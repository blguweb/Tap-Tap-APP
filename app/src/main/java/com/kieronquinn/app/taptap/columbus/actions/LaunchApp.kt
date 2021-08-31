package com.kieronquinn.app.taptap.columbus.actions

import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import com.google.android.systemui.columbus.actions.Action
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.kieronquinn.app.taptap.TapAccessibilityService
import com.kieronquinn.app.taptap.activities.mBluetooth
import com.kieronquinn.app.taptap.activities.misuser
import com.kieronquinn.app.taptap.utils.isAppLaunchable

class LaunchApp(context: Context, private val appPackageName: String) : ActionBase(context) {

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return context.isAppLaunchable(appPackageName) && accessibilityService.getCurrentPackageName() != appPackageName
    }

    private fun getIMU(): String {
        var smessage: String = ""
        //偏航 和 俯仰决定
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, misuser.acc, misuser.gra)

        // Express the updated rotation matrix as three orientation angles.
        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        if (orientationAngles.size>=3) {
            Log.e("z",Math.toDegrees(orientationAngles[0].toDouble())
                .toFloat().toString())
            Log.e("x",Math.toDegrees(orientationAngles[1].toDouble())
                .toFloat().toString())
            Log.e("y",Math.toDegrees(orientationAngles[2].toDouble())
                .toFloat().toString())

            var z = (Math.toDegrees(orientationAngles[0].toDouble()).toFloat() + 720) % 360
            smessage = "$z " + Math.toDegrees(orientationAngles[1].toDouble())
                .toFloat().toString() + ' ' + Math.toDegrees(orientationAngles[2].toDouble())
                .toFloat().toString()
        }
        return smessage
    }

    override fun onTrigger() {
        super.onTrigger()
        val packageManager = context.packageManager
        try {
//            val launchIntent = packageManager.getLaunchIntentForPackage(appPackageName)
//            context.startActivity(launchIntent)
    //            get IMU
            var sm = getIMU()
            if(sm != ""){
                mBluetooth.funBlueClientSend(sm)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}