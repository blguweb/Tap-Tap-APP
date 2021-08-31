package com.kieronquinn.app.taptap.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class BluetoothService {

    companion object {
        var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var mBluetoothSocket: BluetoothSocket? = null
        lateinit var mBluetoothAdapter: BluetoothAdapter
        var isBlueConnected: Boolean = false
        const val MESSAGE_RECEIVE_TAG = 111

        private val BUNDLE_RECEIVE_DATA = "ReceiveData"
        private val TAG = "BlueDeviceActivity"

        //设置发送和接收的字符编码格式
        private val ENCODING_FORMAT = "GBK"
    }
    lateinit var blueAddress: String
    lateinit var blueName: String
    var getMessage =""
    var startcalibration : Int = 0
    lateinit var equipment : String
    //开始连接蓝牙
    fun funStartBlueClientConnect() {
        thread {
            try {
                //这一段代码必须在子线程处理，直接使用协程会阻塞主线程，所以用Thread,其实也可以直接用Thread，不用协程
                if (mBluetoothSocket == null || !isBlueConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = mBluetoothAdapter.getRemoteDevice(blueAddress)
                    mBluetoothSocket =
                        device.createInsecureRfcommSocketToServiceRecord(myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    mBluetoothSocket!!.connect()
                    isBlueConnected = true
                }
            } catch (e: IOException) {
                //连接失败销毁Activity
                e.printStackTrace()
            }
        }
    }

    //打开蓝牙接收消息
    fun funBlueClientStartReceive() {
        thread {
            while (true) {
                //启动蓝牙接收消息
                //注意,如果不在子线程或者协程进行，会导致主线程阻塞，无法绘制
                try {
                    if (mBluetoothSocket != null) {
                        if (mBluetoothSocket!!.isConnected) {
                            Log.e("eee", "现在可以接收数据了")
                            receiveMessage()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "funBlueClientStartReceive:" + e.toString())
                }
            }
        }
    }

    //
//    fun receiveMessage() {
//        Log.i(TAG, "BEGIN mConnectedThread")
//        val mmBuffer: ByteArray = ByteArray(1024)
//        val mmInStream: InputStream = mBluetoothSocket!!.inputStream
//        var bytes = 0 //length
//        var ch
//        // Keep listening to the InputStream while connected
//        while (isBlueConnected) {
//            try {
//                bytes = 0
//                while ((ch = mmInStrea'm.read()) != '\n) {
//                    if (ch != -1) {
//                        mmBuffer[bytes] = (byte) ch//将读取到的字符写入bytest+;
//                        bytes++;
//                    }
//                }
//                buffer[bytes] = (byte) '\n';//最后再加上一个换行bytes++;
//                bytes++;
//            }
//
////            mHandler.obtainMessage(Constants.MESSAGB_READ， bytes，arg2: -1，buffer)
////            .sendToTarget();
//        } catch (IOException e){
//            Log.e(TAG, "disconnected")
//            connectLost()
//            break;
//        }
//    }

//    fun receiveMessage(){
//        val mmInStream: InputStream = mBluetoothSocket!!.inputStream
//        val buffer = ByteArray(2048)
//        var readBytes = 0
//        val stringBuilder = StringBuilder()
//        while (mmInStream.read(buffer).also { readBytes = it } > 0) {
//            Log.e(TAG, readBytes.toString())
//            stringBuilder.append(String(buffer, 0, readBytes))
//        }
//        println(stringBuilder.toString())
//    }
    @Throws(IOException::class)
    open fun readInputStream(inputStream: InputStream): ByteArray? {
        val buffer = ByteArray(1024)
        var len = 0
        val bos = ByteArrayOutputStream()
        while (inputStream.read(buffer).also { len = it } != -1) {
            bos.write(buffer, 0, len)
        }
        bos.close()
        return bos.toByteArray()
    }

//    fun receiveMessage(){
//        val minputStream: InputStream = mBluetoothSocket!!.inputStream
//
//        val getData = readInputStream(minputStream)
//        minputStream.read(getData)
//        val str = String(getData!!)
//        println(str)
////        val mmInStream: InputStream = mBluetoothSocket!!.inputStream
////        val buffer = ByteArray(1024)
////        var len = 0
////        val bos = ByteArrayOutputStream()
////        println("打印内容：$len")
////        while (mmInStream.read(buffer).also({ len = it }) != -1) {
////            println("打印内容")
////            bos.write(buffer, 0, len)
////        }
////        bos.close()
////        val getData= bos.toByteArray()
////        println("打印内容2")
//////        mmInStream.read(getData)
////        val str = String(getData)
////        println("打印内容3")
////        println("打印内容：$str")
//    }

        //蓝牙接收消息的函数体
    fun receiveMessage() {
        val mmInStream: InputStream = mBluetoothSocket!!.inputStream
        val mmBuffer: ByteArray = ByteArray(2048) // mmBuffer store for the stream
        var bytes = 0
        var stringMessage = ""
        //java.lang.OutOfMemoryError: pthread_create (1040KB stack) failed: Try again
        //已经Thread了就不要再次thread了
        //   thread {
        while (true) {

            // Read from the InputStream.
            try {
                var length = mmInStream.read(mmBuffer)
                if(length != -1)
                {
                    bytes = mmInStream.read(mmBuffer)
                }

            } catch (e: IOException) {
                Log.d(TAG, "Input stream was disconnected", e)
                break
            }
            val message = Message()
            val bundle = Bundle()
            //默认GBK编码
            val string = String(mmBuffer, 0, bytes, Charset.forName(ENCODING_FORMAT))
            bundle.putString(BUNDLE_RECEIVE_DATA, string)
            message.what = MESSAGE_RECEIVE_TAG
            message.data = bundle
            handler.sendMessage(message)
            Log.e("receive", string)
            getMessage = chooseEquipment(string)
            Log.e("receiveddd", getMessage)
            println("output = ${string}")

        }
    }



    // 获得设备
    private fun chooseEquipment(equipment: String): String {
        return when (equipment) {
            "0" -> "television"
            "1" -> "airconditioner"
            "2" -> "audio"
            "3" -> "fan"
            "4" -> "sweeper"
            else -> equipment
        }
    }

    //蓝牙发送消息
    fun funBlueClientSend(input: String) {
        if (mBluetoothSocket != null && isBlueConnected) {
            try {
                mBluetoothSocket!!.outputStream.write(input.toByteArray(Charset.forName(ENCODING_FORMAT)))
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "sendCommand: 发送消息失败", e)
            }
        }
    }

    //这是官方推荐的方法
    private val handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            val stringBuffer = StringBuffer()
            lateinit var string : StringBuffer
//            var stringMessage:String = ""
//            var stringmessage : StringBuffer
            when (msg.what) {
                MESSAGE_RECEIVE_TAG -> {
                    string = stringBuffer.append(msg.data.getString(BUNDLE_RECEIVE_DATA))
                }
            }

//            funBlueClientSend(string.toString())
        }
    }

    //蓝牙断开连接
    fun disconnect() {
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket!!.close()
                mBluetoothSocket = null
                isBlueConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "disconnect: 蓝牙关闭失败", e)
            }
        }
    }

    //获取系统时间
    @SuppressLint("SimpleDateFormat")
    private fun funGetSystemTime(): String {
        val simpleFormatter = SimpleDateFormat("YYYY.MM.dd HH:mm:ss")
        val date = Date(System.currentTimeMillis())
        return simpleFormatter.format(date)
    }
}