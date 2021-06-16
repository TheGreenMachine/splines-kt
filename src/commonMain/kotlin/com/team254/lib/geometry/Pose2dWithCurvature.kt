package com.team254.lib.geometry

import com.team254.lib.util.Util

class Pose2dWithCurvature : com.team254.lib.geometry.IPose2d<Pose2dWithCurvature>,
    com.team254.lib.geometry.ICurvature<Pose2dWithCurvature?> {
    protected val pose_: com.team254.lib.geometry.Pose2d
    override val curvature: Double
    override val dCurvatureDs: Double

    constructor() {
        pose_ = com.team254.lib.geometry.Pose2d()
        curvature = 0.0
        dCurvatureDs = 0.0
    }

    constructor(pose: com.team254.lib.geometry.Pose2d, curvature: Double) {
        pose_ = pose
        this.curvature = curvature
        dCurvatureDs = 0.0
    }

    constructor(pose: com.team254.lib.geometry.Pose2d, curvature: Double, dcurvature_ds: Double) {
        pose_ = pose
        this.curvature = curvature
        dCurvatureDs = dcurvature_ds
    }

    constructor(
        translation: com.team254.lib.geometry.Translation2d,
        rotation: com.team254.lib.geometry.Rotation2d,
        curvature: Double
    ) {
        pose_ = com.team254.lib.geometry.Pose2d(translation, rotation)
        this.curvature = curvature
        dCurvatureDs = 0.0
    }

    constructor(
        translation: com.team254.lib.geometry.Translation2d,
        rotation: com.team254.lib.geometry.Rotation2d,
        curvature: Double,
        dcurvature_ds: Double
    ) {
        pose_ = com.team254.lib.geometry.Pose2d(translation, rotation)
        this.curvature = curvature
        dCurvatureDs = dcurvature_ds
    }

    override val pose: com.team254.lib.geometry.Pose2d
        get() = pose_

    override fun transformBy(transform: com.team254.lib.geometry.Pose2d): Pose2dWithCurvature {
        return Pose2dWithCurvature(pose.transformBy(transform), curvature, dCurvatureDs)
    }

    override fun mirror(): Pose2dWithCurvature {
        return Pose2dWithCurvature(pose.mirror().getPose(), -curvature, -dCurvatureDs)
    }

    override val translation: com.team254.lib.geometry.Translation2d
        get() = pose.getTranslation()
    override val rotation: com.team254.lib.geometry.Rotation2d
        get() = pose.getRotation()

    fun interpolate(other: Pose2dWithCurvature, x: Double): Pose2dWithCurvature {
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
        val p2dwc = other
        return pose == p2dwc.pose && Util.epsilonEquals(curvature, p2dwc.curvature) && Util.epsilonEquals(
            dCurvatureDs,
            p2dwc.dCurvatureDs
        )
    }

    override fun toString(): String {
        val fmt = DecimalFormat("#0.000")
        return pose.toString() + ", curvature: " + fmt.format(curvature) + ", dcurvature_ds: " + fmt.format(dCurvatureDs)
    }

    override fun toCSV(): String {
        val fmt = DecimalFormat("#0.000")
        return pose.toCSV() + "," + fmt.format(curvature) + "," + fmt.format(dCurvatureDs)
    }

    companion object {
        protected val kIdentity = Pose2dWithCurvature()
        fun identity(): Pose2dWithCurvature {
            return kIdentity
        }
    }
}