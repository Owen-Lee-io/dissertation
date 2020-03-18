package com.yousuf.ppglogger

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.KeyEvent
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : WearableActivity() {
    val mPPGListener = PPGListener()
    val TAG = "MainActivity"
    var timestamp : Timestamp? = null
    var recording = false
    var mSensorManager : SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorList: List<Sensor> = mSensorManager!!.getSensorList(Sensor.TYPE_ALL)
        for (currentSensor in sensorList) {
            Log.d("List sensors", "Name: " + currentSensor.name.toString() + " /Type_String: " + currentSensor.stringType.toString() + " /Type_number: " + currentSensor.type)
        }
        setAmbientEnabled()
    }
    fun writePPGData(data: FloatArray){
        val filePath = applicationContext.filesDir.absolutePath + File.separator + "$timestamp.csv"
        var file = File(filePath)
        var writer : CSVWriter

        writer = if(file.exists() && !file.isDirectory) {
            var fileWriter = FileWriter(filePath, true)
            CSVWriter(fileWriter, ',', CSVWriter.NO_QUOTE_CHARACTER)
        } else {
            CSVWriter(FileWriter(filePath),',', CSVWriter.NO_QUOTE_CHARACTER)
        }

        for(point in data){
            var row = arrayOf(point.toString())
            writer.writeNext(row)
        }
        Log.d(TAG, "Written PPG data to $timestamp.csv")
        writer.close();
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.repeatCount == 0) {
            when (keyCode) {
                KeyEvent.KEYCODE_STEM_1 -> {
                    recording = !recording
                    if(recording){
                        Log.d(TAG, "Beginning to record PPG data")
                        timestamp = Timestamp(Date().time)
                        mSensorManager!!.registerListener(mPPGListener,
                                mSensorManager!!.getDefaultSensor(65572),
                                SensorManager.SENSOR_DELAY_FASTEST)
                    }else{
                        Log.d(TAG, "Stopping recording PPG data and writing output")
                        // We've stopped recording so write the output to a file
                        mSensorManager?.unregisterListener(mPPGListener)
                        writePPGData(mPPGListener.mData.toFloatArray())
                    }
                    true
                }
                else -> {
                    super.onKeyDown(keyCode, event)
                }
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    class PPGListener : SensorEventListener {
        val TAG = "PPGListener"
        var mData : ArrayList<Float> = ArrayList()
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

        override fun onSensorChanged(event: SensorEvent?) {
//            Log.d(TAG, event?.values!![0].toRawBits().toFloat().toString())
            mData.add(event?.values!![0].toRawBits().toFloat())
        }

    }

}