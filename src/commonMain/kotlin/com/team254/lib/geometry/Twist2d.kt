package com.team254.lib.geometry

import com.team254.lib.util.Util

/**
 * A movement along an arc at constant curvature and velocity. We can use ideas from "differential calculus" to create
 * new RigidTransform2d's from a Twist2d and visa versa.
 *
 *
 * A Twist can be used to represent a difference between two poses, a velocity, an acceleration, etc.
 */
class Twist2d(
    val dx: Double, val dy: Double, // Radians!
    val dtheta: Double
) {
    fun scaled(scale: Double): Twist2d {
        return Twist2d(dx * scale, dy * scale, dtheta * scale)
    }

    fun norm(): Double {
        // Common case of dy == 0
        return if (dy == 0.0) kotlin.math.abs(dx) else kotlin.math.hypot(dx, dy)
    }

    fun curvature(): Double {
        return if (kotlin.math.abs(dtheta) < Util.kEpsilon && norm() < Util.kEpsilon) 0.0 else dtheta / norm()
    }

    override fun toString(): String {
        val fmt = DecimalFormat("#0.000")
        return "(" + fmt.format(dx) + "," + fmt.format(dy) + "," + fmt.format(kotlin.math.toDegrees(dtheta)) + " deg)"
    }

    companion object {
        protected val kIdentity = Twist2d(0.0, 0.0, 0.0)
        fun identity(): Twist2d {
            return kIdentity
        }
    }
}