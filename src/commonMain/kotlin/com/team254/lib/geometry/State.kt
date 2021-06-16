package com.team254.lib.geometry

import com.team254.lib.util.CSVWritable

interface State<S> : Interpolable<S>, CSVWritable {
    fun distance(other: S): Double
    override fun equals(other: Any?): Boolean
    override fun toString(): String
    fun toCSV(): String
}