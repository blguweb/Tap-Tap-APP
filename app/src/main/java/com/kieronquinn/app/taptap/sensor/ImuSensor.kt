package com.kieronquinn.app.taptap.sensor

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService

class ImuSensor (var msensormanager : SensorManager) : SensorEventListener{

    var acc = FloatArray(3)
    var gra = FloatArray(3)
    var acc_sen : Sensor ?= null
    var gra_sen : Sensor ?= null

    fun init(){
        acc_sen = msensormanager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gra_sen = msensormanager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if(acc_sen != null){
            msensormanager!!.registerListener(this,acc_sen,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        }

        //地磁传感器
        if(gra_sen != null){
            msensormanager!!.registerListener(this,gra_sen,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        }
    }




    override fun onSensorChanged(event: SensorEvent?) {
        var values = event?.values
        if (event!!.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc = values!!.clone()
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            gra = values!!.clone()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}