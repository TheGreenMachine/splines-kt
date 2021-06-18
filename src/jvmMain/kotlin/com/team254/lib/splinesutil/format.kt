package com.team254.lib.splinesutil

actual fun Double.format(places: Int): String {
    return String.format("%.${places}f", this)
}