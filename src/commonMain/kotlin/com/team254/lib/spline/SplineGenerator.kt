package com.team254.lib.spline

import com.team254.lib.geometry.Pose2d
import com.team254.lib.geometry.Pose2d.Companion.log
import com.team254.lib.geometry.Pose2dWithCurvature
import com.team254.lib.geometry.Rotation2d
import com.team254.lib.geometry.Translation2d
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

object SplineGenerator {
    private const val kMaxDX = 2.0 //inches
    private const val kMaxDY = 0.05 //inches
    private const val kMaxDTheta = 0.1 //radians!
    private const val kMinSampleSize = 1
    /**
     * Converts a spline into a list of Twist2d's.
     *
     * @param s  the spline to parametrize
     * @param t0 starting percentage of spline to parametrize
     * @param t1 ending percentage of spline to parametrize
     * @return list of Pose2dWithCurvature that approximates the original spline
     */
    /**
     * Convenience function to parametrize a spline from t 0 to 1
     */
    @JvmOverloads
    @JvmStatic
    fun parameterizeSpline(
        s: Spline,
        maxDx: Double = kMaxDX,
        maxDy: Double = kMaxDY,
        maxDTheta: Double = kMaxDTheta,
        t0: Double = 0.0,
        t1: Double = 1.0
    ): List<Pose2dWithCurvature> {
        val rv: MutableList<Pose2dWithCurvature> = ArrayList()
        rv.add(s.getPose2dWithCurvature(0.0))
        val dt = t1 - t0
        var t = 0.0
        while (t < t1) {
            getSegmentArc(s, rv, t, t + dt / kMinSampleSize, maxDx, maxDy, maxDTheta)
            t += dt / kMinSampleSize
        }
        return rv
    }

    @JvmStatic
    fun parameterizeSplines(splines: List<Spline>): List<Pose2dWithCurvature> {
        return parameterizeSplines(splines, kMaxDX, kMaxDY, kMaxDTheta)
    }

    @JvmStatic
    fun parameterizeSplines(
        splines: List<Spline>, maxDx: Double, maxDy: Double,
        maxDTheta: Double
    ): List<Pose2dWithCurvature> {
        val rv: MutableList<Pose2dWithCurvature> = ArrayList()
        if (splines.isEmpty()) return rv
        rv.add(splines[0].getPose2dWithCurvature(0.0))
        for (s in splines) {
            val samples = parameterizeSpline(s, maxDx, maxDy, maxDTheta)
            rv.addAll(samples.drop(1))
        }
        return rv
    }

    private fun getSegmentArc(
        s: Spline, rv: MutableList<Pose2dWithCurvature>, t0: Double, t1: Double, maxDx: Double,
        maxDy: Double,
        maxDTheta: Double
    ) {
        val p0: Translation2d = s.getPoint(t0)
        val p1: Translation2d = s.getPoint(t1)
        val r0: Rotation2d = s.getHeading(t0)
        val r1: Rotation2d = s.getHeading(t1)
        val transformation = Pose2d(Translation2d(p0, p1).rotateBy(r0.inverse()), r1.rotateBy(r0.inverse()))
        val twist = log(transformation)
        if (twist.dy > maxDy || twist.dx > maxDx || twist.dtheta > maxDTheta) {
            getSegmentArc(s, rv, t0, (t0 + t1) / 2, maxDx, maxDy, maxDTheta)
            getSegmentArc(s, rv, (t0 + t1) / 2, t1, maxDx, maxDy, maxDTheta)
        } else {
            rv.add(s.getPose2dWithCurvature(t1))
        }
    }
}