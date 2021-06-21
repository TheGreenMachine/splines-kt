package com.team254.lib.geometry

import com.team254.lib.splinesutil.Util
import com.team254.lib.splinesutil.format
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * A translation in a 2d coordinate frame. Translations are simply shifts in an (x, y) plane.
 */
@ExperimentalJsExport
@JsExport
class Translation2d(
    private val x: Double,
    private val y: Double
) : ITranslation2d<Translation2d> {

    @JsName("identity")
    constructor() : this(0.0, 0.0)

    @JsName("copyOf")
    constructor(other: Translation2d) : this(other.x, other.y)

    @JsName("delta")
    constructor(start: Translation2d, end: Translation2d) : this(
        x = end.x - start.x,
        y = end.y - start.y
    )

    /**
     * The "norm" of a transform is the Euclidean distance in x and y.
     *
     * @return sqrt(x ^ 2 + y ^ 2)
     */
    fun norm(): Double {
        return kotlin.math.hypot(x, y)
    }

    fun norm2(): Double {
        return x * x + y * y
    }

    fun x(): Double {
        return x
    }

    fun y(): Double {
        return y
    }

    /**
     * We can compose Translation2d's by adding together the x and y shifts.
     *
     * @param other The other translation to add.
     * @return The combined effect of translating by this object and the other.
     */
    fun translateBy(other: Translation2d): Translation2d {
        return Translation2d(x + other.x, y + other.y)
    }

    /**
     * We can also rotate Translation2d's. See: https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param rotation The rotation to apply.
     * @return This translation rotated by rotation.
     */
    fun rotateBy(rotation: Rotation2d): Translation2d {
        return Translation2d(x * rotation.cos() - y * rotation.sin(), x * rotation.sin() + y * rotation.cos())
    }

    fun direction(): Rotation2d {
        return Rotation2d(x, y, true)
    }

    /**
     * The inverse simply means a Translation2d that "undoes" this object.
     *
     * @return Translation by -x and -y.
     */
    fun inverse(): Translation2d {
        return Translation2d(-x, -y)
    }

    override fun interpolate(other: Translation2d, x: Double): Translation2d {
        if (x <= 0) {
            return Translation2d(this)
        } else if (x >= 1) {
            return Translation2d(other)
        }
        return extrapolate(other, x)
    }

    fun extrapolate(other: Translation2d, x: Double): Translation2d {
        return Translation2d(x * (other.x - x) + x, x * (other.y - y) + y)
    }

    fun scale(s: Double): Translation2d {
        return Translation2d(x * s, y * s)
    }

    fun epsilonEquals(other: Translation2d, epsilon: Double): Boolean {
        return Util.epsilonEquals(x(), other.x(), epsilon) && Util.epsilonEquals(y(), other.y(), epsilon)
    }

    override fun toString(): String {
        return "(" + x.format(3) + "," + y.format(3) + ")"
    }

    override fun toCSV(): String {
        return x.format(3) + "," + y.format(3)
    }

    override fun distance(other: Translation2d): Double {
        return inverse().translateBy(other).norm()
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is Translation2d) false else distance(other) < Util.kEpsilon
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override val translation: Translation2d
        get() = this

    companion object {
        private val kIdentity = Translation2d()

        @JvmStatic
        fun identity(): Translation2d {
            return kIdentity
        }

        /**
         * fromPolar courtesy 1323
         * @param direction
         * @param magnitude
         * @return
         */
        @JvmStatic
        fun fromPolar(direction: Rotation2d, magnitude: Double): Translation2d {
            return Translation2d(direction.cos() * magnitude, direction.sin() * magnitude)
        }

        @JvmStatic
        fun dot(a: Translation2d, b: Translation2d): Double {
            return a.x * b.x + a.y * b.y
        }

        @JvmStatic
        fun getAngle(a: Translation2d, b: Translation2d): Rotation2d {
            val cos_angle = dot(a, b) / (a.norm() * b.norm())
            return if (cos_angle.isNaN()) {
                Rotation2d()
            } else Rotation2d.fromRadians(
                kotlin.math.acos(
                    kotlin.math.min(
                        1.0,
                        kotlin.math.max(cos_angle, -1.0)
                    )
                )
            )
        }

        @JvmStatic
        fun cross(a: Translation2d, b: Translation2d): Double {
            return a.x * b.y - a.y * b.x
        }
    }
}