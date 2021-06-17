package com.team254.lib.geometry

interface ICurvature<S: Any> : State<S> {
    val curvature: Double
    val dCurvatureDs: Double
}