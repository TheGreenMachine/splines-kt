package com.team254.lib.splinesutil

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmOverloads
import kotlin.math.PI

/**
 * Contains basic functions that are used often.
 */
object Util {
    const val kEpsilon = 1e-12

    /**
     * Limits the given input to the given magnitude.
     */
    fun limit(v: Double, maxMagnitude: Double): Double {
        return limit(v, -maxMagnitude, maxMagnitude)
    }

    fun limit(v: Double, min: Double, max: Double): Double {
        return kotlin.math.min(max, kotlin.math.max(min, v))
    }

    fun interpolate(a: Double, b: Double, x: Double): Double {
        var x = x
        x = limit(x, 0.0, 1.0)
        return a + (b - a) * x
    }

    fun joinStrings(delim: String?, strings: List<*>): String {
        val sb = StringBuilder()
        for (i in strings.indices) {
            sb.append(strings[i].toString())
            if (i < strings.size - 1) {
                sb.append(delim)
            }
        }
        return sb.toString()
    }

    @JvmOverloads
    fun epsilonEquals(a: Double, b: Double, epsilon: Double = kEpsilon): Boolean {
        return a - epsilon <= b && a + epsilon >= b
    }

    fun epsilonEquals(a: Int, b: Int, epsilon: Int): Boolean {
        return a - epsilon <= b && a + epsilon >= b
    }

    fun allCloseTo(list: List<Double>, value: Double, epsilon: Double): Boolean {
        var result = true
        for (value_in in list) {
            result = result and epsilonEquals(value_in, value, epsilon)
        }
        return result
    }
}

@ExperimentalJsExport
@JsName("toDegrees")
fun toDegrees(radians: Double): Double {
    return radians * (180.0 / PI)
}

@ExperimentalJsExport
@JsName("toRadians")
fun toRadians(degrees: Double): Double {
    return degrees * (PI / 180.0)
}

expect fun Double.format(places: Int): String