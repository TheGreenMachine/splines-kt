package com.team254.lib.geometry

interface IPose2d<S> : com.team254.lib.geometry.IRotation2d<S>, com.team254.lib.geometry.ITranslation2d<S> {
    val pose: com.team254.lib.geometry.Pose2d
    fun transformBy(transform: com.team254.lib.geometry.Pose2d): S
    fun mirror(): S
}