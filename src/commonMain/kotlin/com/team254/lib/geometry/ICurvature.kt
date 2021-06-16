package com.team254.lib.geometry

interface ICurvature<S> : com.team254.lib.geometry.State<S> {
    val curvature: Double
    val dCurvatureDs: Double
}