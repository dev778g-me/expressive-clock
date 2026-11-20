package com.dev.expressiveclock.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.dev.expressiveclock.data.HandStyle
import kotlin.math.roundToInt

object HandRenderer {

    fun render(
        style: HandStyle,
        colour: Int,
        rMin: Float,
        lengthR: Float,
        widthR: Float,
        tailR: Float,
        pinDiameterR: Float,
        pinColour: Int,
    ): Bitmap {
        val tip = lengthR * rMin
        val half = maxOf(tip, tailR * rMin) + widthR * rMin
        val side = (2f * half).roundToInt().coerceAtLeast(2)
        val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        if (style == HandStyle.OFF) return bitmap // fully transparent; see HandStyle.OFF

        val canvas = Canvas(bitmap)
        val cx = side / 2f
        val cy = side / 2f
        val w = widthR * rMin
        val tail = tailR * rMin

        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        fill.color = colour
        fill.style = Paint.Style.FILL

        when (style) {
            HandStyle.BATON -> canvas.drawRoundRect(
                cx - w / 2f, cy - tip, cx + w / 2f, cy + tail, w / 2f, w / 2f, fill,
            )

            HandStyle.NEEDLE -> canvas.drawRoundRect(
                cx - w / 4f, cy - tip, cx + w / 4f, cy + tail, w / 4f, w / 4f, fill,
            )

            HandStyle.TAPER -> canvas.drawPath(
                Path().apply {
                    moveTo(cx, cy - tip)
                    lineTo(cx + w / 2f, cy - tip * 0.15f)
                    lineTo(cx + w / 2.6f, cy + tail)
                    lineTo(cx - w / 2.6f, cy + tail)
                    lineTo(cx - w / 2f, cy - tip * 0.15f)
                    close()
                },
                fill,
            )

            HandStyle.DIAMOND -> canvas.drawPath(
                Path().apply {
                    moveTo(cx, cy - tip)
                    lineTo(cx + w / 2f, cy - tip * 0.4f)
                    lineTo(cx, cy + tail)
                    lineTo(cx - w / 2f, cy - tip * 0.4f)
                    close()
                },
                fill,
            )

            HandStyle.OUTLINE -> canvas.drawRoundRect(
                cx - w / 2f, cy - tip, cx + w / 2f, cy + tail, w / 2f, w / 2f,
                Paint(fill).also {
                    it.style = Paint.Style.STROKE
                    it.strokeWidth = w * 0.28f
                },
            )

            HandStyle.BALL -> {
                val ballR = w * 1.5f
                canvas.drawRoundRect(
                    cx - w / 4f, cy - tip, cx + w / 4f, cy + tail, w / 4f, w / 4f, fill,
                )
                canvas.drawCircle(cx, cy - tip + ballR * 2.1f, ballR, fill)
            }

            /** Disc at the very tip with no overhang: the Braun/Rams seconds hand. */
            HandStyle.DOT -> {
                val ballR = w * 1.2f
                canvas.drawRoundRect(
                    cx - w / 6f, cy - tip + ballR * 2f, cx + w / 6f, cy + tail, w / 6f, w / 6f, fill,
                )
                canvas.drawCircle(cx, cy - tip + ballR, ballR, fill)
            }

            /** An open ring at the end of a stick. Reads as a marker rather than a pointer. */
            HandStyle.RING -> {
                val ringR = w * 1.7f
                val stroke = Paint(fill).also {
                    it.style = Paint.Style.STROKE
                    it.strokeWidth = w * 0.45f
                }
                canvas.drawRoundRect(
                    cx - w / 5f, cy - tip + ringR * 2f, cx + w / 5f, cy + tail, w / 5f, w / 5f, fill,
                )
                canvas.drawCircle(cx, cy - tip + ringR, ringR, stroke)
            }

            /** The dress-watch leaf, pointed at both ends and widest in the middle. */
            HandStyle.LEAF -> canvas.drawPath(
                Path().apply {
                    moveTo(cx, cy - tip)
                    quadTo(cx + w * 0.85f, cy - tip * 0.45f, cx, cy + tail)
                    quadTo(cx - w * 0.85f, cy - tip * 0.45f, cx, cy - tip)
                    close()
                },
                fill,
            )

            /** A straight stick with a triangular head, for a legible hour hand. */
            HandStyle.ARROW -> {
                val headLen = tip * 0.32f
                canvas.drawRoundRect(
                    cx - w / 3f, cy - tip + headLen, cx + w / 3f, cy + tail, w / 3f, w / 3f, fill,
                )
                canvas.drawPath(
                    Path().apply {
                        moveTo(cx, cy - tip)
                        lineTo(cx + w * 0.9f, cy - tip + headLen)
                        lineTo(cx - w * 0.9f, cy - tip + headLen)
                        close()
                    },
                    fill,
                )
            }

            HandStyle.OFF -> Unit
        }

        if (pinDiameterR > 0f) {
            canvas.drawCircle(
                cx, cy, pinDiameterR * rMin / 2f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pinColour },
            )
        }
        return bitmap
    }

    /** A 1×1 transparent bitmap, which is what an "off" hour or minute hand has to be. */
    fun blank(): Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}
