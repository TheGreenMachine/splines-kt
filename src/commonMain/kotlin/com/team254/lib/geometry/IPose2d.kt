package com.team254.lib.geometry

interface IPose2d<S: Any> : IRotation2d<S>, ITranslation2d<S> {
    val pose: Pose2d
    fun transformBy(other: Pose2d): S
    fun mirror(): S
}