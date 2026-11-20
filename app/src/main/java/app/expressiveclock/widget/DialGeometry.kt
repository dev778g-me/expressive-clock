package app.expressiveclock.widget

import app.expressiveclock.data.IndexSet
import app.expressiveclock.data.NumeralSize

object DialGeometry {

    const val HOUR_LENGTH = 0.778f
    const val MINUTE_LENGTH = 0.889f
    const val SECOND_LENGTH = 0.820f

    const val HOUR_WIDTH = 0.0889f
    const val MINUTE_WIDTH = 0.0711f
    const val SECOND_WIDTH = 0.0356f

    /** The tail behind the pin. The thinnest hand carries the longest one, as on a real watch. */
    const val HOUR_TAIL = 0.067f
    const val MINUTE_TAIL = 0.0889f
    const val SECOND_TAIL = 0.156f

    const val PIN_DIAMETER = 0.0625f

    const val NUMERAL_RING = 0.82f
    const val MINOR_DOT_RADIUS = 0.028f
    const val TICK_LENGTH = 0.055f
    const val TICK_LENGTH_MINOR = 0.030f

    const val MIN_RING_FOR_TWELVE = 0.716f

    fun arcSpacing(indexSet: IndexSet, ringR: Float): Float {
        val n = indexSet.hours.size.coerceAtLeast(1)
        return (2.0 * Math.PI * ringR / n).toFloat()
    }

    fun clampedCap(size: NumeralSize, indexSet: IndexSet, ringR: Float, widestInkR: Float): Float {
        if (widestInkR <= 0f) return size.capR
        val room = arcSpacing(indexSet, ringR) * 0.9f
        return minOf(size.capR, room / widestInkR)
    }
}
