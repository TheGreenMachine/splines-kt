package com.team254.lib.geometry

import com.team254.lib.util.Util
import com.team254.lib.util.Util.kEpsilon
import com.team254.lib.util.format
import com.team254.lib.util.toDegrees
import com.team254.lib.util.toRadians
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * A rotation in a 2d coordinate frame represented a point on the unit circle (cosine and sine).
 *
 *
 * Inspired by Sophus (https://github.com/strasdat/Sophus/tree/master/sophus)
 */
class Rotation2d : IRotation2d<Rotation2d> {
    private val cos_angle_: Double
    private val sin_angle_: Double

    @JvmOverloads
    constructor(x: Double = 1.0, y: Double = 0.0, normalize: Boolean = false) {
        if (normalize) {
            // From trig, we know that sin^2 + cos^2 == 1, but as we do math on this object we might accumulate rounding errors.
            // Normalizing forces us to re-scale the sin and cos to reset rounding errors.
            val magnitude: Double = kotlin.math.hypot(x, y)
            if (magnitude > kEpsilon) {
                sin_angle_ = y / magnitude
                cos_angle_ = x / magnitude
            } else {
                sin_angle_ = 0.0
                cos_angle_ = 1.0
            }
        } else {
            cos_angle_ = x
            sin_angle_ = y
        }
    }

    constructor(other: Rotation2d) {
        cos_angle_ = other.cos_angle_
        sin_angle_ = other.sin_angle_
    }

    constructor(direction: Translation2d, normalize: Boolean) : this(
        direction.x(),
        direction.y(),
        normalize
    )

    fun cos(): Double {
        return cos_angle_
    }

    fun sin(): Double {
        return sin_angle_
    }

    fun tan(): Double {
        return if (kotlin.math.abs(cos_angle_) < kEpsilon) {
            if (sin_angle_ >= 0.0) {
                Double.POSITIVE_INFINITY
            } else {
                Double.NEGATIVE_INFINITY
            }
        } else sin_angle_ / cos_angle_
    }

    val radians: Double
        get() = kotlin.math.atan2(sin_angle_, cos_angle_)
    val degrees: Double
        get() = toDegrees(radians)

    /**
     * We can rotate this Rotation2d by adding together the effects of it and another rotation.
     *
     * @param other The other rotation. See: https://en.wikipedia.org/wiki/Rotation_matrix
     * @return This rotation rotated by other.
     */
    fun rotateBy(other: Rotation2d): Rotation2d {
        return Rotation2d(
            cos_angle_ * other.cos_angle_ - sin_angle_ * other.sin_angle_,
            cos_angle_ * other.sin_angle_ + sin_angle_ * other.cos_angle_, true
        )
    }

    fun normal(): Rotation2d {
        return Rotation2d(-sin_angle_, cos_angle_, false)
    }

    /**
     * The inverse of a Rotation2d "undoes" the effect of this rotation.
     *
     * @return The opposite of this rotation.
     */
    fun inverse(): Rotation2d {
        return Rotation2d(cos_angle_, -sin_angle_, false)
    }

    fun isParallel(other: Rotation2d): Boolean {
        return Util.epsilonEquals(
            Translation2d.cross(
                toTranslation(),
                other.toTranslation()
            ), 0.0
        )
    }

    fun toTranslation(): Translation2d {
        return Translation2d(cos_angle_, sin_angle_)
    }

    /**
     * Based on Team 1323's method of the same name.
     *
     * @return Rotation2d representing the angle of the nearest axis to the angle in standard position
     */
    fun nearestPole(): Rotation2d {
        val pole_sin: Double
        val pole_cos: Double
        if (kotlin.math.abs(cos_angle_) > kotlin.math.abs(sin_angle_)) {
            pole_cos = kotlin.math.sign(cos_angle_)
            pole_sin = 0.0
        } else {
            pole_cos = 0.0
            pole_sin = kotlin.math.sign(sin_angle_)
        }
        return Rotation2d(pole_cos, pole_sin, false)
    }

    override fun interpolate(other: Rotation2d, x: Double): Rotation2d {
        if (x <= 0) {
            return Rotation2d(this)
        } else if (x >= 1) {
            return Rotation2d(other)
        }
        val angle_diff = inverse().rotateBy(other).radians
        return rotateBy(fromRadians(angle_diff * x))
    }

    override fun toString(): String {
        return "(" + degrees.format(3) + " deg)"
    }

    override fun toCSV(): String {
        return degrees.format(3)
    }

    override fun distance(other: Rotation2d): Double {
        return inverse().rotateBy(other).radians
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is Rotation2d) false else distance(other) < kEpsilon
    }

    override fun hashCode(): Int {
        var result = cos_angle_.hashCode()
        result = 31 * result + sin_angle_.hashCode()
        return result
    }

    override val rotation: Rotation2d
        get() = this

    companion object {
        private val kIdentity = Rotation2d()

        @JvmStatic
        fun identity(): Rotation2d {
            return kIdentity
        }

        @JvmStatic
        fun fromRadians(angle_radians: Double): Rotation2d {
            return Rotation2d(kotlin.math.cos(angle_radians), kotlin.math.sin(angle_radians), false)
        }

        @JvmStatic
        fun fromDegrees(angle_degrees: Double): Rotation2d {
            return fromRadians(toRadians(angle_degrees))
        }
    }
}