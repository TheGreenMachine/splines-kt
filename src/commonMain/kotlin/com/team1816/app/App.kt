package com.team1816.app

import com.team254.lib.spline.QuinticHermiteSpline
import kotlin.jvm.JvmStatic

class App {
    @Export("calcSplines")
    fun calcSplines(message: String): String {
        var message = message
        message = message.substring(0, message.length - 1)
        try {
            message = URLDecoder.decode(message, java.nio.charset.StandardCharsets.UTF_8)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val points: java.util.ArrayList<Pose2d> = java.util.ArrayList<Pose2d>()
        for (pointString in message.split(";").toTypedArray()) {
            val pointData = pointString.split(",").toTypedArray()
            val x = if (pointData[0] == "NaN") 0 else pointData[0].toInt()
            val y = if (pointData[1] == "NaN") 0 else pointData[1].toInt()
            val heading = if (pointData[2] == "NaN") 0 else pointData[2].toInt()
            points.add(Pose2d(Translation2d(x, y), Rotation2d.fromDegrees(heading)))
        }
        val mQuinticHermiteSplines: java.util.ArrayList<QuinticHermiteSpline> =
            java.util.ArrayList<QuinticHermiteSpline>()
        val mSplines: java.util.ArrayList<Spline> = java.util.ArrayList<Spline>()
        val positions: java.util.ArrayList<Pose2dWithCurvature> = java.util.ArrayList<Pose2dWithCurvature>()
        if (points.size < 2) {
            return "no"
        } else {
            for (i in 0 until points.size - 1) {
                mQuinticHermiteSplines.add(QuinticHermiteSpline(points.get(i), points.get(i + 1)))
            }
            QuinticHermiteSpline.optimizeSpline(mQuinticHermiteSplines)
            for (mQuinticHermiteSpline in mQuinticHermiteSplines) {
                mSplines.add(mQuinticHermiteSpline)
            }
            positions.addAll(SplineGenerator.parameterizeSplines(mSplines))
        }
        val json = StringBuilder("{\"points\":[")
        for (pose in positions) {
            json.append(poseToJSON(pose)).append(",")
        }
        return json.substring(0, json.length - 1) + "]}"
    }

    private fun poseToJSON(pose: Pose2dWithCurvature): String {
        val x: Double = pose.translation.x()
        val y: Double = pose.translation.y()
        val rotation: Double = pose.rotation.radians
        val curvature: Double = pose.curvature
        return "{\"x\":$x, \"y\":$y, \"rotation\":$rotation, \"curvature\":$curvature}"
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Spline library loaded!")
        }
    }
}