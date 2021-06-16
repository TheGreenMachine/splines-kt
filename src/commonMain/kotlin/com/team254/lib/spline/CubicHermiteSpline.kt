package com.team254.lib.spline

import com.team254.lib.geometry.Pose2d
import com.team254.lib.geometry.Rotation2d
import com.team254.lib.geometry.Translation2d

/**
 * Temporary spline for testing
 */
class CubicHermiteSpline(p0: Pose2d, p1: Pose2d) : com.team254.lib.spline.Spline() {
    private val ax: Double
    private val bx: Double
    private val cx: Double
    private val dx: Double
    private val ay: Double
    private val by: Double
    private val cy: Double
    private val dy: Double
    override fun getPoint(t: Double): Translation2d {
        val x = t * t * t * ax + t * t * bx + t * cx + dx
        val y = t * t * t * ay + t * t * by + t * cy + dy
        return Translation2d(x, y)
    }

    override fun getHeading(t: Double): Rotation2d {
        val dx = 3 * t * t * ax + 2 * t * bx + cx
        val dy = 3 * t * t * ay + 2 * t * by + cy
        return Rotation2d(dx, dy, true)
    }

    override fun getVelocity(t: Double): Double {
        // TODO implement this
        return 1.0
    }

    override fun getCurvature(t: Double): Double {
        val dx = 3 * t * t * ax + 2 * t * bx + cx
        val dy = 3 * t * t * ay + 2 * t * by + cy
        val ddx = 6 * t * ax + 2 * bx
        val ddy = 6 * t * ay + 2 * by
        return (dx * ddy - dy * ddx) / ((dx * dx + dy * dy) * kotlin.math.sqrt(dx * dx + dy * dy))
    }

    override fun getDCurvature(t: Double): Double {
        // TODO implement this
        return 0.0
    }

    init {
        val dx0: Double
        val dx1: Double
        val dy0: Double
        val dy1: Double
        val scale = 2 * p0.translation.distance(p1.translation)
        val x0: Double = p0.translation.x()
        val x1: Double = p1.translation.x()
        dx0 = p0.rotation.cos() * scale
        dx1 = p1.rotation.cos() * scale
        val y0: Double = p0.translation.y()
        val y1: Double = p1.translation.y()
        dy0 = p0.rotation.sin() * scale
        dy1 = p1.rotation.sin() * scale
        ax = dx0 + dx1 + 2 * x0 - 2 * x1
        bx = -2 * dx0 - dx1 - 3 * x0 + 3 * x1
        cx = dx0
        dx = x0
        ay = dy0 + dy1 + 2 * y0 - 2 * y1
        by = -2 * dy0 - dy1 - 3 * y0 + 3 * y1
        cy = dy0
        dy = y0
    }
}