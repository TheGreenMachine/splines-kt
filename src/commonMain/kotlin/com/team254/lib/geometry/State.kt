package com.team254.lib.geometry

import com.team254.lib.util.CSVWritable
import com.team254.lib.util.Interpolable

interface State<S: Any> : Interpolable<S>, CSVWritable {
    fun distance(other: S): Double
    override fun equals(other: Any?): Boolean
    override fun toString(): String
    override fun toCSV(): String
}