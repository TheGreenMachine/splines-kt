package com.team254.lib.spline

import com.team254.lib.geometry.Pose2d
import com.team254.lib.geometry.Pose2dWithCurvature
import com.team254.lib.geometry.Rotation2d
import com.team254.lib.geometry.Translation2d

abstract class Spline {
    abstract fun getPoint(t: Double): Translation2d
    abstract fun getHeading(t: Double): Rotation2d
    abstract fun getCurvature(t: Double): Double

    // dk/ds
    abstract fun getDCurvature(t: Double): Double

    // ds/dt
    abstract fun getVelocity(t: Double): Double
    fun getPose2d(t: Double): Pose2d {
        return Pose2d(getPoint(t), getHeading(t))
    }

    fun getPose2dWithCurvature(t: Double): Pose2dWithCurvature {
        return Pose2dWithCurvature(getPose2d(t), getCurvature(t), getDCurvature(t) / getVelocity(t))
    } // TODO add toString
    // public abstract String toString();
}