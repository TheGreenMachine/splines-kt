import com.team254.lib.geometry.Pose2d
import com.team254.lib.geometry.Pose2dWithCurvature
import com.team254.lib.spline.QuinticHermiteSpline
import com.team254.lib.spline.Spline
import com.team254.lib.spline.SplineGenerator

external fun decodeURIComponent(encodedURI: String): String

@ExperimentalJsExport
@JsExport
@JsName("calcSplines")
fun calcSplines(points: Array<Pose2d>): String {
    val mQuinticHermiteSplines: ArrayList<QuinticHermiteSpline> =
        ArrayList()
    val mSplines: ArrayList<Spline> = ArrayList()
    val positions: ArrayList<Pose2dWithCurvature> = ArrayList()
    if (points.size < 2) {
        return "no"
    } else {
        for (i in 0 until points.size - 1) {
            mQuinticHermiteSplines.add(QuinticHermiteSpline(points[i], points[i + 1]))
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

fun main() {
    println("Spline library loaded!")
}