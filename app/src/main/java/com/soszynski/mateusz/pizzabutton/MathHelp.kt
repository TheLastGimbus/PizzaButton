package com.soszynski.mateusz.pizzabutton

import kotlin.math.roundToInt

class MathHelp {
    // yup, my own map function from arduino because i was to lazy to search deeper for
    // builtin equivalent in kotlin
    fun map(x: Double, in_min: Double, in_max: Double, out_min: Double, out_max: Double): Double {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
    }

    fun voltageToPercentage(voltage: Double): Int {
        return map(
                voltage,
                3.0,
                4.2,
                0.0,
                100.0
        ).roundToInt()
    }
}