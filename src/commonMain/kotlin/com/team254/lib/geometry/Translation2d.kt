package com.team254.lib.geometry

import com.team254.lib.util.Util

/**
 * A translation in a 2d coordinate frame. Translations are simply shifts in an (x, y) plane.
 */
class Translation2d : com.team254.lib.geometry.ITranslation2d<Translation2d?> {
    protected val x_: Double
    protected val y_: Double

    constructor() {
        x_ = 0.0
        y_ = 0.0
    }

    constructor(x: Double, y: Double) {
        x_ = x
        y_ = y
    }

    constructor(other: Translation2d) {
        x_ = other.x_
        y_ = other.y_
    }

    constructor(start: Translation2d, end: Translation2d) {
        x_ = end.x_ - start.x_
        y_ = end.y_ - start.y_
    }

    /**
     * The "norm" of a transform is the Euclidean distance in x and y.
     *
     * @return sqrt(x ^ 2 + y ^ 2)
     */
    fun norm(): Double {
        return kotlin.math.hypot(x_, y_)
    }

    fun norm2(): Double {
        return x_ * x_ + y_ * y_
    }

    fun x(): Double {
        return x_
    }

    fun y(): Double {
        return y_
    }

    /**
     * We can compose Translation2d's by adding together the x and y shifts.
     *
     * @param other The other translation to add.
     * @return The combined effect of translating by this object and the other.
     */
    fun translateBy(other: Translation2d): Translation2d {
        return Translation2d(x_ + other.x_, y_ + other.y_)
    }

    /**
     * We can also rotate Translation2d's. See: https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotation The rotation to apply.
     * @return This translation rotated by rotation.
     */
    fun rotateBy(rotation: com.team254.lib.geometry.Rotation2d): Translation2d {
        return Translation2d(x_ * rotation.cos() - y_ * rotation.sin(), x_ * rotation.sin() + y_ * rotation.cos())
    }

    fun direction(): com.team254.lib.geometry.Rotation2d {
        return com.team254.lib.geometry.Rotation2d(x_, y_, true)
    }

    /**
     * The inverse simply means a Translation2d that "undoes" this object.
     *
     * @return Translation by -x and -y.
     */
    fun inverse(): Translation2d {
        return Translation2d(-x_, -y_)
    }

    fun interpolate(other: Translation2d, x: Double): Translation2d {
        if (x <= 0) {
            return Translation2d(this)
        } else if (x >= 1) {
            return Translation2d(other)
        }
        return extrapolate(other, x)
    }

    fun extrapolate(other: Translation2d, x: Double): Translation2d {
        return Translation2d(x * (other.x_ - x_) + x_, x * (other.y_ - y_) + y_)
    }

    fun scale(s: Double): Translation2d {
        return Translation2d(x_ * s, y_ * s)
    }

    fun epsilonEquals(other: Translation2d, epsilon: Double): Boolean {
        return Util.epsilonEquals(x(), other.x(), epsilon) && Util.epsilonEquals(y(), other.y(), epsilon)
    }

    override fun toString(): String {
        val fmt = DecimalFormat("#0.000")
        return "(" + fmt.format(x_) + "," + fmt.format(y_) + ")"
    }

    override fun toCSV(): String {
        val fmt = DecimalFormat("#0.000")
        return fmt.format(x_) + "," + fmt.format(y_)
    }

    override fun distance(other: Translation2d): Double {
        return inverse().translateBy(other).norm()
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is Translation2d) false else distance(other) < Util.kEpsilon
    }

    override val translation: Translation2d
        get() = this

    companion object {
        protected val kIdentity = Translation2d()
        fun identity(): Translation2d {
            return kIdentity
        }

        fun dot(a: Translation2d, b: Translation2d): Double {
            return a.x_ * b.x_ + a.y_ * b.y_
        }

        fun getAngle(a: Translation2d, b: Translation2d): com.team254.lib.geometry.Rotation2d {
            val cos_angle = dot(a, b) / (a.norm() * b.norm())
            return if (java.lang.Double.isNaN(cos_angle)) {
                com.team254.lib.geometry.Rotation2d()
            } else com.team254.lib.geometry.Rotation2d.Companion.fromRadians(
                kotlin.math.acos(
                    kotlin.math.min(
                        1.0,
                        kotlin.math.max(cos_angle, -1.0)
                    )
                )
            )
        }

        fun cross(a: Translation2d, b: Translation2d): Double {
            return a.x_ * b.y_ - a.y_ * b.x_
        }
    }
}