package com.team254.lib.geometry

import com.team254.lib.util.Util
import kotlin.jvm.JvmStatic

/**
 * Represents a 2d pose (rigid transform) containing translational and rotational elements.
 *
 *
 * Inspired by Sophus (https://github.com/strasdat/Sophus/tree/master/sophus)
 */
class Pose2d : IPose2d<Pose2d> {
    private val translation_: Translation2d
    private val rotation_: Rotation2d

    constructor() {
        translation_ = Translation2d()
        rotation_ = Rotation2d()
    }

    constructor(x: Double, y: Double, rotation: Rotation2d) {
        translation_ = Translation2d(x, y)
        rotation_ = rotation
    }

    constructor(translation: Translation2d, rotation: Rotation2d) {
        translation_ = translation
        rotation_ = rotation
    }

    constructor(other: Pose2d) {
        translation_ = Translation2d(other.translation_)
        rotation_ = Rotation2d(other.rotation_)
    }

    override val translation: Translation2d
        get() = translation_
    override val rotation: Rotation2d
        get() = rotation_

    /**
     * Transforming this RigidTransform2d means first translating by other.translation and then rotating by
     * other.rotation
     *
     * @param other The other transform.
     * @return This transform * other
     */
    override fun transformBy(other: Pose2d): Pose2d {
        return Pose2d(
            translation_.translateBy(other.translation_.rotateBy(rotation_)),
            rotation_.rotateBy(other.rotation_)
        )
    }

    /**
     * The inverse of this transform "undoes" the effect of translating by this transform.
     *
     * @return The opposite of this transform.
     */
    fun inverse(): Pose2d {
        val rotation_inverted: Rotation2d = rotation_.inverse()
        return Pose2d(translation_.inverse().rotateBy(rotation_inverted), rotation_inverted)
    }

    fun normal(): Pose2d {
        return Pose2d(translation_, rotation_.normal())
    }

    /**
     * Finds the point where the heading of this pose intersects the heading of another. Returns (+INF, +INF) if
     * parallel.
     */
    fun intersection(other: Pose2d): Translation2d {
        val other_rotation: Rotation2d = other.rotation
        if (rotation_.isParallel(other_rotation)) {
            // Lines are parallel.
            return Translation2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        }
        return if (kotlin.math.abs(rotation_.cos()) < kotlin.math.abs(other_rotation.cos())) {
            intersectionInternal(this, other)
        } else {
            intersectionInternal(other, this)
        }
    }

    /**
     * Return true if this pose is (nearly) colinear with the another.
     */
    fun isColinear(other: Pose2d): Boolean {
        if (!rotation.isParallel(other.rotation)) return false
        val twist: Twist2d = log(inverse().transformBy(other))
        return Util.epsilonEquals(twist.dy, 0.0) && Util.epsilonEquals(twist.dtheta, 0.0)
    }

    fun epsilonEquals(other: Pose2d, epsilon: Double): Boolean {
        return (translation.epsilonEquals(other.translation, epsilon)
                && rotation.isParallel(other.rotation))
    }

    /**
     * Do twist interpolation of this pose assuming constant curvature.
     */
    override fun interpolate(other: Pose2d, x: Double): Pose2d {
        if (x <= 0) {
            return Pose2d(this)
        } else if (x >= 1) {
            return Pose2d(other)
        }
        val twist: Twist2d = log(inverse().transformBy(other))
        return transformBy(exp(twist.scaled(x)))
    }

    override fun toString(): String {
        return "T:$translation_, R:$rotation_"
    }

    override fun toCSV(): String {
        return translation_.toCSV() + "," + rotation_.toCSV()
    }

    override fun distance(other: Pose2d): Double {
        return log(inverse().transformBy(other)).norm()
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is Pose2d) false else epsilonEquals(other, Util.kEpsilon)
    }

    override val pose: Pose2d
        get() = this

    override fun mirror(): Pose2d {
        return Pose2d(Translation2d(translation.x(), -translation.y()), rotation.inverse())
    }

    companion object {
        private val kIdentity = Pose2d()
        fun identity(): Pose2d {
            return kIdentity
        }

        private const val kEps = 1E-9
        fun fromTranslation(translation: Translation2d): Pose2d {
            return Pose2d(translation, Rotation2d())
        }

        fun fromRotation(rotation: Rotation2d): Pose2d {
            return Pose2d(Translation2d(), rotation)
        }

        /**
         * Obtain a new Pose2d from a (constant curvature) velocity. See:
         * https://github.com/strasdat/Sophus/blob/master/sophus/se2.hpp
         */
        fun exp(delta: Twist2d): Pose2d {
            val sin_theta: Double = kotlin.math.sin(delta.dtheta)
            val cos_theta: Double = kotlin.math.cos(delta.dtheta)
            val s: Double
            val c: Double
            if (kotlin.math.abs(delta.dtheta) < kEps) {
                s = 1.0 - 1.0 / 6.0 * delta.dtheta * delta.dtheta
                c = .5 * delta.dtheta
            } else {
                s = sin_theta / delta.dtheta
                c = (1.0 - cos_theta) / delta.dtheta
            }
            return Pose2d(
                Translation2d(delta.dx * s - delta.dy * c, delta.dx * c + delta.dy * s),
                Rotation2d(cos_theta, sin_theta, false)
            )
        }

        /**
         * Logical inverse of the above.
         */
        @JvmStatic
        fun log(transform: Pose2d): Twist2d {
            val dtheta: Double = transform.rotation.radians
            val half_dtheta = 0.5 * dtheta
            val cos_minus_one: Double = transform.rotation.cos() - 1.0
            val halftheta_by_tan_of_halfdtheta: Double = if (kotlin.math.abs(cos_minus_one) < kEps) {
                1.0 - 1.0 / 12.0 * dtheta * dtheta
            } else {
                -(half_dtheta * transform.rotation.sin()) / cos_minus_one
            }
            val translation_part: Translation2d = transform.translation
                .rotateBy(Rotation2d(halftheta_by_tan_of_halfdtheta, -half_dtheta, false))
            return Twist2d(translation_part.x(), translation_part.y(), dtheta)
        }

        private fun intersectionInternal(a: Pose2d, b: Pose2d): Translation2d {
            val a_r: Rotation2d = a.rotation
            val b_r: Rotation2d = b.rotation
            val a_t: Translation2d = a.translation
            val b_t: Translation2d = b.translation
            val tan_b: Double = b_r.tan()
            val t: Double = (((a_t.x() - b_t.x()) * tan_b + b_t.y() - a_t.y())
                    / (a_r.sin() - a_r.cos() * tan_b))
            return if (t.isNaN()) {
                Translation2d(
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY
                )
            } else a_t.translateBy(a_r.toTranslation().scale(t))
        }
    }
}