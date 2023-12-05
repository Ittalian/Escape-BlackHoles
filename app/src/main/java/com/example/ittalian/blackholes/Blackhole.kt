package com.example.ittalian.blackholes

class Blackhole(val x: Float, val y: Float, val speed: Int) {
    val max = 400f
    val min = 30f
    var r = 30f
    var sign = 1

    fun grow() {
        if (r > max)
            sign = -1
        else if (r < min)
            sign = 1

        r += (speed * sign).toFloat()
    }
}