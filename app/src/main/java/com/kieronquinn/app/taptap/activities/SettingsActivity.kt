package com.kieronquinn.app.taptap.activities

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.equipment.*
import com.kieronquinn.app.taptap.aispeak.TTS
import com.kieronquinn.app.taptap.fragments.BaseFragment
import com.kieronquinn.app.taptap.sensor.ImuSensor
import com.kieronquinn.app.taptap.utils.BluetoothService
import com.kieronquinn.app.taptap.utils.animateColorChange
import com.kieronquinn.app.taptap.utils.animateElevationChange
import com.kieronquinn.app.taptap.utils.dip
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.android.synthetic.main.activity_settings.*
import kotlin.concurrent.thread

var mBluetooth = BluetoothService()
//IMU
lateinit var  misuser : ImuSensor

class SettingsActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private var stringTwo : String = "请从第二个位置指示设备位置并Tap Tap"
    private var calibrationSuccess : String = "标定成功！"
    private var calibrationFailed : String = "标定失败，请从第一个位置指示设备位置并Tap Tap！"
    private val isLightTheme
        get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

//    //初始化变量
//    companion object {
//        var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//        var mBluetoothSocket: BluetoothSocket? = null
//        lateinit var mBluetoothAdapter: BluetoothAdapter
//        var isBlueConnected: Boolean = false
//        const val MESSAGE_RECEIVE_TAG = 111
//        lateinit var blueAddress: String
//        lateinit var blueName: String
//        private val BUNDLE_RECEIVE_DATA = "ReceiveData"
//        private val TAG = "BlueDeviceActivity"
//
//        //设置发送和接收的字符编码格式
//        private val ENCODING_FORMAT = "GBK"
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setHomeAsUpIndicator(R.drawable.ic_back)
        }
        navController.addOnDestinationChangedListener(this)
        Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
        if (isLightTheme) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                    .or(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        toolbar.applySystemWindowInsetsToPadding(top = true)
        setToolbarElevationEnabled(false)

        //IMU
        var sensorManager : SensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        misuser = ImuSensor(sensorManager)
        misuser.init()

        //获取蓝牙设备的名字和地址
        mBluetooth.blueAddress = intent.getStringExtra(MainActivity.BLUE_ADDRESS)!!
        mBluetooth.blueName = intent.getStringExtra(MainActivity.BLUE_NAME)!!

        mBluetooth.funStartBlueClientConnect()
        //打开蓝牙接收消息

        mBluetooth.funBlueClientStartReceive()

        receiveAction()

        //点击发送消息
//        val stringBuffer = StringBuffer()
//        var toString = "hello"
////            if (check_blue_add_newline.isChecked) {
////                toString += "\r\n"
////            }
//        if (isBlueConnected) {
//            stringBuffer.append("发送的消息:" + toString)
//            Log.e(TAG, stringBuffer.toString())"APP1"
//        } else {
//            stringBuffer.append("发送失败，蓝牙连接已经断开")
//            Log.e(TAG, stringBuffer.toString())
//        }

    }


    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        toolbar_title?.text = destination.label
    }

    private var isToolbarElevationEnabled = false
    private var toolbarColorAnimation: ValueAnimator? = null
    private var toolbarElevationAnimation: ValueAnimator? = null



    fun setToolbarElevationEnabled(enabled: Boolean){
        if(enabled == isToolbarElevationEnabled) return
        isToolbarElevationEnabled = enabled
        val toolbarColor = if(enabled){
            ContextCompat.getColor(this, R.color.toolbarColor)
        }else{
            ContextCompat.getColor(this, R.color.windowBackground)
        }
        val elevation = if(enabled) dip(8).toFloat() else 0f
        val initialBeforeColor = if(toolbarColorAnimation == null){
            ContextCompat.getColor(this, R.color.toolbarColor)
        }else{
            null
        }

        toolbarColorAnimation?.cancel()
        toolbarElevationAnimation?.cancel()
        toolbarColorAnimation = toolbar.animateColorChange(beforeColor = initialBeforeColor, afterColor = toolbarColor)
        toolbarElevationAnimation = toolbar.animateElevationChange(elevation)
    }

    private fun receiveAction(){
        thread {
            while (true){
                if(mBluetooth.getMessage != ""){
                    when(mBluetooth.getMessage){
                        "television" -> {
                            val intent = Intent()
                            //获取intent对象
                            intent.setClass(this, television::class.java)
                            // 获取class是使用::反射
                            startActivity(intent)
                            mBluetooth.getMessage = ""
                        }
                        "airconditioner" -> {
                            val intent = Intent()
                            //获取intent对象
                            intent.setClass(this, airConditioner::class.java)
                            // 获取class是使用::反射
                            startActivity(intent)
                            mBluetooth.getMessage = ""
                        }
                        "audio" -> {
                            val intent = Intent()
                            //获取intent对象
                            intent.setClass(this, audio::class.java)
                            // 获取class是使用::反射
                            startActivity(intent)
                            mBluetooth.getMessage = ""
                        }
                        "fan" -> {
                            val intent = Intent()
                            //获取intent对象
                            intent.setClass(this, fan::class.java)
                            // 获取class是使用::反射
                            startActivity(intent)
                            mBluetooth.getMessage = ""
                        }
                        "sweeper" -> {
                            val intent = Intent()
                            //获取intent对象
                            intent.setClass(this, sweeper::class.java)
                            // 获取class是使用::反射
                            startActivity(intent)
                            mBluetooth.getMessage = ""
                        }
                        "a" ->{
                            TTS(this, stringTwo, true)
                            mBluetooth.getMessage = ""
                            mBluetooth.startcalibration = 2
                        }
                        "b" ->{
                            TTS(this, calibrationSuccess, true)
                            mBluetooth.getMessage = ""
                            mBluetooth.startcalibration = 0
                        }
                        "c" ->{
                            TTS(this, calibrationFailed, true)
                            mBluetooth.getMessage = ""
                            mBluetooth.startcalibration = 1
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                navController.navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (navHostFragment?.childFragmentManager != null) {
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            val result = (currentFragment as? BaseFragment)?.onBackPressed()
            if (result != true) super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

}