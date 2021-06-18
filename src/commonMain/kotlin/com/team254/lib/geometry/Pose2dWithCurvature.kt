package com.team254.lib.geometry

import com.team254.lib.util.Util
import com.team254.lib.util.format
import kotlin.jvm.JvmStatic

class Pose2dWithCurvature : IPose2d<Pose2dWithCurvature>,
    ICurvature<Pose2dWithCurvature> {
    private val pose_: Pose2d
    override val curvature: Double
    override val dCurvatureDs: Double

    constructor() {
        pose_ = Pose2d()
        curvature = 0.0
        dCurvatureDs = 0.0
    }

    constructor(pose: Pose2d, curvature: Double) {
        pose_ = pose
        this.curvature = curvature
        dCurvatureDs = 0.0
    }

    constructor(pose: Pose2d, curvature: Double, dcurvature_ds: Double) {
        pose_ = pose
        this.curvature = curvature
        dCurvatureDs = dcurvature_ds
    }

    constructor(
        translation: Translation2d,
        rotation: Rotation2d,
        curvature: Double
    ) {
        pose_ = Pose2d(translation, rotation)
        this.curvature = curvature
        dCurvatureDs = 0.0
    }

    constructor(
        translation: Translation2d,
        rotation: Rotation2d,
        curvature: Double,
        dcurvature_ds: Double
    ) {
        pose_ = Pose2d(translation, rotation)
        this.curvature = curvature
        dCurvatureDs = dcurvature_ds
    }

    override val pose: Pose2d
        get() = pose_

    override fun transformBy(other: Pose2d): Pose2dWithCurvature {
        return Pose2dWithCurvature(pose.transformBy(other), curvature, dCurvatureDs)
    }

    override fun mirror(): Pose2dWithCurvature {
        return Pose2dWithCurvature(pose.mirror().pose, -curvature, -dCurvatureDs)
    }

    override val translation: Translation2d
        get() = pose.translation
    override val rotation: Rotation2d
        get() = pose.rotation

    override fun interpolate(other: Pose2dWithCurvature, x: Double): Pose2dWithCurvature {
        return Pose2dWithCurvature(
            pose.interpolate(other.pose, x),
            Util.interpolate(curvature, other.curvature, x),
            Util.interpolate(dCurvatureDs, other.dCurvatureDs, x)
        )
    }

    override fun distance(other: Pose2dWithCurvature): Double {
        return pose.distance(other.pose)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Pose2dWithCurvature) return false
        return pose == other.pose && Util.epsilonEquals(curvature, other.curvature) && Util.epsilonEquals(
            dCurvatureDs,
            other.dCurvatureDs
        )
    }

    override fun toString(): String {
        return "$pose, curvature: ${curvature.format(3)}, dcurvature_ds: ${dCurvatureDs.format(3)}"
    }

    override fun toCSV(): String {
        return "${pose.toCSV()},${curvature.format(3)},${dCurvatureDs.format(3)}"
    }

    override fun hashCode(): Int {
        var result = pose_.hashCode()
        result = 31 * result + curvature.hashCode()
        result = 31 * result + dCurvatureDs.hashCode()
        return result
    }

    companion object {
        private val kIdentity = Pose2dWithCurvature()

        @JvmStatic
        fun identity(): Pose2dWithCurvature {
            return kIdentity
        }
    }
}