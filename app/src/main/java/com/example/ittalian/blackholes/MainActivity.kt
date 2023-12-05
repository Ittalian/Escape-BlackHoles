package com.example.ittalian.blackholes

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import com.example.ittalian.blackholes.databinding.ActivityMainBinding
import java.lang.Math.floor

class MainActivity : AppCompatActivity(), SensorEventListener, SurfaceHolder.Callback {
    private val tag = "Black holes"
    private val matrixSize = 16
    private val numOfBlackhole = 5
    private val radius = 30f
    private val limitOfBlackhole = 100
    private var mgValues = FloatArray(3)
    private var acValues = FloatArray(3)
    private var startTime: Long = 0
    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var ballX = 0f
    private var ballY = 0f
    private var isGoal = false
    private var isGone = false
    private val blackholesList = ArrayList<Blackhole>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val holder = binding.surfaceView.holder
        holder.addCallback(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val inR = FloatArray(matrixSize)
        val outR = FloatArray(matrixSize)
        val I = FloatArray(matrixSize)
        val orValues = FloatArray(3)

        if (event == null)
            return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> acValues = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> mgValues = event.values.clone()
        }

        SensorManager.getRotationMatrix(inR, I, acValues, mgValues)
        SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR)
        SensorManager.getOrientation(outR, orValues)

        val pitch = rad2Deg(orValues[1])
        val roll = rad2Deg(orValues[2])
        Log.v(tag, "pitch${pitch}")
        Log.v(tag, "roll${roll}")

        if (!isGoal && !isGone)
            drawGameBoard(pitch, roll)
    }

    private fun rad2Deg(rad: Float): Int {
        return kotlin.math.floor(Math.toDegrees(rad.toDouble())).toInt()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magField, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        ballX = (width / 2).toFloat()
        ballY = (height - radius)
        startTime = System.currentTimeMillis()
        bornBlackholes()
    }

    private fun bornBlackholes() {
        for (i in 1..numOfBlackhole) {
            val x: Float = (limitOfBlackhole..surfaceWidth - limitOfBlackhole).random().toFloat()
            val y: Float = (limitOfBlackhole..surfaceHeight - limitOfBlackhole).random().toFloat()
            val speed: Int = (2..11).random()
            val bh = Blackhole(x, y, speed)
            blackholesList.add(bh)
        }
    }

    private fun drawGameBoard(pitch: Int, roll: Int) {
        ballX += roll
        ballY -= pitch

        if (ballX - radius < 0) {
            ballX = radius
        } else if (ballX + radius > surfaceWidth) {
            ballX = surfaceWidth - radius
        }

        if (ballY + radius < 0) {
            isGoal = true
        } else if (ballY + radius > surfaceHeight) {
            ballY = surfaceHeight - radius
        }

        for (bh in blackholesList) {
            if (checkGone(bh.x, bh.y, bh.r))
                isGone = true
        }

        val canvas = binding.surfaceView.holder.lockCanvas()
        val paint = Paint()
        //テスト用
//        paint.color = Color.YELLOW
//        canvas.drawColor(Color.BLUE)
//        canvas.drawCircle(ballX, ballY, radius, paint)
        canvas.drawColor(Color.BLUE)
        paint.color = Color.BLACK

        for (bh in blackholesList){
            canvas.drawCircle(bh.x, bh.y, bh.r, paint)
            bh.grow()
        }

        paint.color = Color.YELLOW

        if (!isGone){
            canvas.drawCircle(ballX, ballY, radius, paint)
        } else {
            paint.color = Color.RED
            paint.textSize = 80f
            canvas.drawText("Game Over", (surfaceWidth / 2).toFloat() - paint.textSize * 2.2f, (surfaceHeight / 2).toFloat(), paint)
        }

        if (isGoal) {
            paint.textSize = 80f
            canvas.drawText(goaled(), (surfaceWidth / 2).toFloat() - paint.textSize * 1.7f, (surfaceHeight / 2).toFloat(), paint)
        }

        binding.surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    private fun checkGone(x0: Float, y0: Float, r: Float): Boolean {
        return (x0 - ballX) * (x0 - ballX) + (y0 - ballY) * (y0 - ballY) < r * r
    }

    private fun goaled(): String {
        val escapedTime = System.currentTimeMillis() - startTime
        val secTime = (escapedTime / 1000).toInt()
        return "Goal ${secTime}秒"
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }
}