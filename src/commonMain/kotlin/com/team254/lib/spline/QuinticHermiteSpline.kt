package com.team254.lib.spline

import com.team254.lib.geometry.Pose2d
import com.team254.lib.geometry.Rotation2d
import com.team254.lib.geometry.Translation2d

class QuinticHermiteSpline : Spline {
    private var x0: Double
    private var x1: Double
    private var dx0: Double
    private var dx1: Double
    private var ddx0: Double
    private var ddx1: Double
    private var y0: Double
    private var y1: Double
    private var dy0: Double
    private var dy1: Double
    private var ddy0: Double
    private var ddy1: Double
    private var ax = 0.0
    private var bx = 0.0
    private var cx = 0.0
    private var dx = 0.0
    private var ex = 0.0
    private var fx = 0.0
    private var ay = 0.0
    private var by = 0.0
    private var cy = 0.0
    private var dy = 0.0
    private var ey = 0.0
    private var fy = 0.0

    /**
     * @param p0 The starting pose of the spline
     * @param p1 The ending pose of the spline
     */
    constructor(p0: Pose2d, p1: Pose2d) {
        val scale = 1.2 * p0.translation.distance(p1.translation)
        x0 = p0.translation.x()
        x1 = p1.translation.x()
        dx0 = p0.rotation.cos() * scale
        dx1 = p1.rotation.cos() * scale
        ddx0 = 0.0
        ddx1 = 0.0
        y0 = p0.translation.y()
        y1 = p1.translation.y()
        dy0 = p0.rotation.sin() * scale
        dy1 = p1.rotation.sin() * scale
        ddy0 = 0.0
        ddy1 = 0.0
        computeCoefficients()
    }

    /**
     * Used by the curvature optimization function
     */
    private constructor(
        x0: Double, x1: Double, dx0: Double, dx1: Double, ddx0: Double, ddx1: Double,
        y0: Double, y1: Double, dy0: Double, dy1: Double, ddy0: Double, ddy1: Double
    ) {
        this.x0 = x0
        this.x1 = x1
        this.dx0 = dx0
        this.dx1 = dx1
        this.ddx0 = ddx0
        this.ddx1 = ddx1
        this.y0 = y0
        this.y1 = y1
        this.dy0 = dy0
        this.dy1 = dy1
        this.ddy0 = ddy0
        this.ddy1 = ddy1
        computeCoefficients()
    }

    /**
     * Re-arranges the spline into an at^5 + bt^4 + ... + f form for simpler computations
     */
    private fun computeCoefficients() {
        ax = -6 * x0 - 3 * dx0 - 0.5 * ddx0 + 0.5 * ddx1 - 3 * dx1 + 6 * x1
        bx = 15 * x0 + 8 * dx0 + 1.5 * ddx0 - ddx1 + 7 * dx1 - 15 * x1
        cx = -10 * x0 - 6 * dx0 - 1.5 * ddx0 + 0.5 * ddx1 - 4 * dx1 + 10 * x1
        dx = 0.5 * ddx0
        ex = dx0
        fx = x0
        ay = -6 * y0 - 3 * dy0 - 0.5 * ddy0 + 0.5 * ddy1 - 3 * dy1 + 6 * y1
        by = 15 * y0 + 8 * dy0 + 1.5 * ddy0 - ddy1 + 7 * dy1 - 15 * y1
        cy = -10 * y0 - 6 * dy0 - 1.5 * ddy0 + 0.5 * ddy1 - 4 * dy1 + 10 * y1
        dy = 0.5 * ddy0
        ey = dy0
        fy = y0
    }

    val startPose: Pose2d
        get() = Pose2d(
            Translation2d(x0, y0),
            Rotation2d(dx0, dy0, true)
        )
    val endPose: Pose2d
        get() = Pose2d(
            Translation2d(x1, y1),
            Rotation2d(dx1, dy1, true)
        )

    /**
     * @param t ranges from 0 to 1
     * @return the point on the spline for that t value
     */
    override fun getPoint(t: Double): Translation2d {
        val x = ax * t * t * t * t * t + bx * t * t * t * t + cx * t * t * t + dx * t * t + ex * t + fx
        val y = ay * t * t * t * t * t + by * t * t * t * t + cy * t * t * t + dy * t * t + ey * t + fy
        return Translation2d(x, y)
    }

    private fun dx(t: Double): Double {
        return 5 * ax * t * t * t * t + 4 * bx * t * t * t + 3 * cx * t * t + 2 * dx * t + ex
    }

    private fun dy(t: Double): Double {
        return 5 * ay * t * t * t * t + 4 * by * t * t * t + 3 * cy * t * t + 2 * dy * t + ey
    }

    private fun ddx(t: Double): Double {
        return 20 * ax * t * t * t + 12 * bx * t * t + 6 * cx * t + 2 * dx
    }

    private fun ddy(t: Double): Double {
        return 20 * ay * t * t * t + 12 * by * t * t + 6 * cy * t + 2 * dy
    }

    private fun dddx(t: Double): Double {
        return 60 * ax * t * t + 24 * bx * t + 6 * cx
    }

    private fun dddy(t: Double): Double {
        return 60 * ay * t * t + 24 * by * t + 6 * cy
    }

    override fun getVelocity(t: Double): Double {
        return kotlin.math.hypot(dx(t), dy(t))
    }

    override fun getCurvature(t: Double): Double {
        return (dx(t) * ddy(t) - ddx(t) * dy(t)) / ((dx(t) * dx(t) + dy(t) * dy(t)) * kotlin.math.sqrt(
            dx(t) * dx(t) + dy(
                t
            ) * dy(t)
        ))
    }

    override fun getDCurvature(t: Double): Double {
        val dx2dy2 = dx(t) * dx(t) + dy(t) * dy(t)
        val num =
            (dx(t) * dddy(t) - dddx(t) * dy(t)) * dx2dy2 - 3 * (dx(t) * ddy(t) - ddx(t) * dy(t)) * (dx(t) * ddx(t) + dy(
                t
            ) * ddy(t))
        return num / (dx2dy2 * dx2dy2 * kotlin.math.sqrt(dx2dy2))
    }

    private fun dCurvature2(t: Double): Double {
        val dx2dy2 = dx(t) * dx(t) + dy(t) * dy(t)
        val num =
            (dx(t) * dddy(t) - dddx(t) * dy(t)) * dx2dy2 - 3 * (dx(t) * ddy(t) - ddx(t) * dy(t)) * (dx(t) * ddx(t) + dy(
                t
            ) * ddy(t))
        return num * num / (dx2dy2 * dx2dy2 * dx2dy2 * dx2dy2 * dx2dy2)
    }

    override fun getHeading(t: Double): Rotation2d {
        return Rotation2d(dx(t), dy(t), true)
    }

    /**
     * @return integral of dCurvature^2 over the length of the spline
     */
    private fun sumDCurvature2(): Double {
        val dt = 1.0 / kSamples
        var sum = 0.0
        var t = 0.0
        while (t < 1.0) {
            sum += dt * dCurvature2(t)
            t += dt
        }
        return sum
    }

    /**
     * Makes optimization code a little more readable
     */
    private class ControlPoint {
        var ddx = 0.0
        var ddy = 0.0
    }

    companion object {
        private const val kEpsilon = 1e-5
        private const val kStepSize = 1.0
        private const val kMinDelta = 0.001
        private const val kSamples = 100
        private const val kMaxIterations = 100

        /**
         * @return integral of dCurvature^2 over the length of multiple splines
         */
        fun sumDCurvature2(splines: List<QuinticHermiteSpline>): Double {
            var sum = 0.0
            for (s in splines) {
                sum += s.sumDCurvature2()
            }
            return sum
        }

        /**
         * Finds the optimal second derivative values for a set of splines to reduce the sum of the change in curvature
         * squared over the path
         *
         * @param splines the list of splines to optimize
         * @return the final sumDCurvature2
         */
        fun optimizeSpline(splines: MutableList<QuinticHermiteSpline>): Double {
            var count = 0
            var prev = sumDCurvature2(splines)
            while (count < kMaxIterations) {
                runOptimizationIteration(splines)
                val current = sumDCurvature2(splines)
                if (prev - current < kMinDelta) return current
                prev = current
                count++
            }
            return prev
        }

        /**
         * Runs a single optimization iteration
         */
        private fun runOptimizationIteration(splines: MutableList<QuinticHermiteSpline>) {
            //can't optimize anything with less than 2 splines
            if (splines.size <= 1) {
                return
            }
            val controlPoints = arrayOfNulls<ControlPoint>(splines.size - 1)
            var magnitude = 0.0
            for (i in 0 until splines.size - 1) {
                //don't try to optimize colinear points
                if (splines[i].startPose.isColinear(splines[i + 1].startPose) || splines[i].endPose.isColinear(splines[i + 1].endPose)) {
                    continue
                }
                val original = sumDCurvature2(splines)
                val temp: QuinticHermiteSpline = splines[i]
                val temp1: QuinticHermiteSpline = splines[i + 1]
                controlPoints[i] = ControlPoint() //holds the gradient at a control point

                //calculate partial derivatives of sumDCurvature2
                splines[i] = QuinticHermiteSpline(
                    temp.x0,
                    temp.x1,
                    temp.dx0,
                    temp.dx1,
                    temp.ddx0,
                    temp.ddx1 +
                            kEpsilon,
                    temp.y0,
                    temp.y1,
                    temp.dy0,
                    temp.dy1,
                    temp.ddy0,
                    temp.ddy1
                )
                splines[i + 1] = QuinticHermiteSpline(
                    temp1.x0,
                    temp1.x1,
                    temp1.dx0,
                    temp1.dx1,
                    temp1.ddx0 +
                            kEpsilon,
                    temp1.ddx1,
                    temp1.y0,
                    temp1.y1,
                    temp1.dy0,
                    temp1.dy1,
                    temp1.ddy0,
                    temp1.ddy1
                )
                controlPoints[i]!!.ddx = (sumDCurvature2(splines) - original) / kEpsilon
                splines[i] = QuinticHermiteSpline(
                    temp.x0,
                    temp.x1,
                    temp.dx0,
                    temp.dx1,
                    temp.ddx0,
                    temp.ddx1,
                    temp.y0,
                    temp.y1,
                    temp.dy0,
                    temp.dy1,
                    temp.ddy0,
                    temp.ddy1 + kEpsilon
                )
                splines[i + 1] = QuinticHermiteSpline(
                    temp1.x0,
                    temp1.x1,
                    temp1.dx0,
                    temp1.dx1,
                    temp1.ddx0,
                    temp1.ddx1,
                    temp1.y0,
                    temp1.y1,
                    temp1.dy0,
                    temp1.dy1,
                    temp1.ddy0 + kEpsilon,
                    temp1.ddy1
                )
                controlPoints[i]!!.ddy = (sumDCurvature2(splines) - original) / kEpsilon
                splines[i] = temp
                splines[i + 1] = temp1
                magnitude += controlPoints[i]!!.ddx * controlPoints[i]!!.ddx + controlPoints[i]!!.ddy * controlPoints[i]!!.ddy
            }
            magnitude = kotlin.math.sqrt(magnitude)

            //minimize along the direction of the gradient
            //first calculate 3 points along the direction of the gradient
            val p1: Translation2d
            val p2: Translation2d
            val p3: Translation2d
            p2 = Translation2d(0.0, sumDCurvature2(splines)) //middle point is at the current location
            for (i in 0 until splines.size - 1) { //first point is offset from the middle location by -stepSize
                if (splines[i].startPose.isColinear(splines[i + 1].startPose) || splines[i].endPose.isColinear(splines[i + 1].endPose)) {
                    continue
                }
                //normalize to step size
                controlPoints[i]!!.ddx *= kStepSize / magnitude
                controlPoints[i]!!.ddy *= kStepSize / magnitude

                //move opposite the gradient by step size amount
                splines[i].ddx1 -= controlPoints[i]!!.ddx
                splines[i].ddy1 -= controlPoints[i]!!.ddy
                splines[i + 1].ddx0 -= controlPoints[i]!!.ddx
                splines[i + 1].ddy0 -= controlPoints[i]!!.ddy

                //recompute the spline's coefficients to account for new second derivatives
                splines[i].computeCoefficients()
                splines[i + 1].computeCoefficients()
            }
            p1 = Translation2d(-kStepSize, sumDCurvature2(splines))
            for (i in 0 until splines.size - 1) { //last point is offset from the middle location by +stepSize
                if (splines[i].startPose.isColinear(splines[i + 1].startPose) || splines[i].endPose.isColinear(splines[i + 1].endPose)) {
                    continue
                }
                //move along the gradient by 2 times the step size amount (to return to original location and move by 1
                // step)
                splines[i].ddx1 += 2 * controlPoints[i]!!.ddx
                splines[i].ddy1 += 2 * controlPoints[i]!!.ddy
                splines[i + 1].ddx0 += 2 * controlPoints[i]!!.ddx
                splines[i + 1].ddy0 += 2 * controlPoints[i]!!.ddy

                //recompute the spline's coefficients to account for new second derivatives
                splines[i].computeCoefficients()
                splines[i + 1].computeCoefficients()
            }
            p3 = Translation2d(kStepSize, sumDCurvature2(splines))
            val stepSize = fitParabola(p1, p2, p3) //approximate step size to minimize sumDCurvature2 along the gradient
            for (i in 0 until splines.size - 1) {
                if (splines[i].startPose.isColinear(splines[i + 1].startPose) || splines[i].endPose.isColinear(splines[i + 1].endPose)) {
                    continue
                }
                //move by the step size calculated by the parabola fit (+1 to offset for the final transformation to find
                // p3)
                controlPoints[i]!!.ddx *= 1 + stepSize / kStepSize
                controlPoints[i]!!.ddy *= 1 + stepSize / kStepSize
                splines[i].ddx1 += controlPoints[i]!!.ddx
                splines[i].ddy1 += controlPoints[i]!!.ddy
                splines[i + 1].ddx0 += controlPoints[i]!!.ddx
                splines[i + 1].ddy0 += controlPoints[i]!!.ddy

                //recompute the spline's coefficients to account for new second derivatives
                splines[i].computeCoefficients()
                splines[i + 1].computeCoefficients()
            }
        }

        /**
         * fits a parabola to 3 points
         *
         * @return the x coordinate of the vertex of the parabola
         */
        private fun fitParabola(p1: Translation2d, p2: Translation2d, p3: Translation2d): Double {
            val A = p3.x() * (p2.y() - p1.y()) + p2.x() * (p1.y() - p3.y()) + p1.x() * (p3.y() - p2.y())
            val B = p3.x() * p3.x() * (p1.y() - p2.y()) + p2.x() * p2.x() * (p3.y() - p1.y()) + p1.x() * p1.x() *
                    (p2.y() - p3.y())
            return -B / (2 * A)
        }
    }
}