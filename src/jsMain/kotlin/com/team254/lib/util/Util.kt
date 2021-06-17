package com.team254.lib.util

actual fun Double.format(places: Int): String {
    return this.asDynamic().toFixed(places) as String
}