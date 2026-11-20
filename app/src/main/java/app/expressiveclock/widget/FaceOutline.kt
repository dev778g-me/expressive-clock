package app.expressiveclock.widget

import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Rect
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

class FaceOutline(path: Path, val cx: Float, val cy: Float) {

    /** The outline as a closed polyline in final pixel coordinates. */
    private val pts: FloatArray = run {
        val approx = path.approximate(APPROX_ERROR_PX)
        FloatArray(2 * (approx.size / 3)) { i ->
            val t = i / 2
            if (i % 2 == 0) approx[t * 3 + 1] else approx[t * 3 + 2]
        }
    }

    /** The smallest distance from the centre to the outline, over a full turn. */
    val rMin: Float = run {
        var m = Float.MAX_VALUE
        for (i in 0 until 360) {
            val hit = castRay(i * Math.PI.toFloat() / 180f)
            if (hit != null) m = min(m, hypot(hit.x - cx, hit.y - cy))
        }
        if (m == Float.MAX_VALUE) 1f else m
    }

    /** A point on the outline, with the inward unit normal there. */
    class Hit(val x: Float, val y: Float, val nx: Float, val ny: Float)

    fun castRay(angle: Float): Hit? {
        val dx = sin(angle)
        val dy = -cos(angle)
        var bestT = -1f
        var bx = 0f
        var by = 0f
        var nx = 0f
        var ny = 0f
        val n = pts.size / 2
        if (n < 2) return null
        for (i in 0 until n) {
            val j = (i + 1) % n
            val ax = pts[i * 2]
            val ay = pts[i * 2 + 1]
            val ex = pts[j * 2]
            val ey = pts[j * 2 + 1]
            val sx = ex - ax
            val sy = ey - ay
            val denom = dx * sy - dy * sx
            if (abs(denom) < 1e-7f) continue
            val t = ((ax - cx) * sy - (ay - cy) * sx) / denom
            val u = ((ax - cx) * dy - (ay - cy) * dx) / denom
            if (t > bestT && u in 0f..1f) {
                bestT = t
                bx = cx + dx * t
                by = cy + dy * t
                val len = hypot(sx, sy).takeIf { it > 1e-7f } ?: 1f
                var px = sy / len
                var py = -sx / len
                if (px * (cx - bx) + py * (cy - by) < 0f) {
                    px = -px
                    py = -py
                }
                nx = px
                ny = py
            }
        }
        return if (bestT <= 0f) null else Hit(bx, by, nx, ny)
    }

    fun placeGlyph(angle: Float, ink: Rect, margin: Float): Hit? {
        val edge = castRay(angle) ?: return null
        val support = (ink.width() / 2f) * abs(edge.nx) + (ink.height() / 2f) * abs(edge.ny)
        val d = margin + support
        return Hit(edge.x + edge.nx * d, edge.y + edge.ny * d, edge.nx, edge.ny)
    }

    fun alongOutline(path: Path, count: Int, inset: Float): List<Hit> {
        val measure = PathMeasure(path, true)
        val total = measure.length
        if (total <= 0f || count <= 0) return emptyList()
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        val startHit = castRay(0f)
        val startD = startHit?.let { nearestDistance(measure, total, it.x, it.y) } ?: 0f
        return (0 until count).map { i ->
            val d = (startD + total * i / count) % total
            measure.getPosTan(d, pos, tan)
            val len = hypot(tan[0], tan[1]).takeIf { it > 1e-7f } ?: 1f
            var nx = tan[1] / len
            var ny = -tan[0] / len
            if (nx * (cx - pos[0]) + ny * (cy - pos[1]) < 0f) {
                nx = -nx
                ny = -ny
            }
            Hit(pos[0] + nx * inset, pos[1] + ny * inset, nx, ny)
        }
    }

    private fun nearestDistance(measure: PathMeasure, total: Float, x: Float, y: Float): Float {
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        var best = 0f
        var bestD = Float.MAX_VALUE
        var d = 0f
        val step = total / 720f
        while (d < total) {
            measure.getPosTan(d, pos, tan)
            val dist = hypot(pos[0] - x, pos[1] - y)
            if (dist < bestD) {
                bestD = dist
                best = d
            }
            d += step
        }
        return best
    }

    private companion object {
        /** Half a pixel: finer than the eye and far finer than any glyph placement needs. */
        const val APPROX_ERROR_PX = 0.5f
    }
}
