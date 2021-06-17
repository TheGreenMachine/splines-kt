package com.team254.lib.util

actual fun Double.format(places: Int): String {
    return String.format("%.${places}f", this)
}