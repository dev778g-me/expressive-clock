package app.expressiveclock.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import app.expressiveclock.data.FaceFill
import app.expressiveclock.data.IndexSet
import app.expressiveclock.data.MinorIndex
import app.expressiveclock.data.NumeralLayout
import app.expressiveclock.data.NumeralSystem
import app.expressiveclock.data.WidgetConfig
import app.expressiveclock.data.WidgetFontFamily
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object FaceRenderer {

    class Colours(
        val face: Int,
        /** Numerals. */
        val onFace: Int,
        /** Dots, rings and ticks. A step down from [onFace] so the numerals still lead. */
        val minor: Int,
        val outline: Int,
        /** The single accent, for previews and thumbnails that draw one representative colour. */
        val accent: Int,
        val hourHand: Int,
        val minuteHand: Int,
        val secondHand: Int,
        val pin: Int,
    )

    class Face(val bitmap: Bitmap, val rMin: Float, val cx: Float, val cy: Float)

    fun render(context: Context, config: WidgetConfig, colours: Colours, w: Int, h: Int): Face {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val wf = w.toFloat()
        val hf = h.toFloat()

        val strokeR = config.outline.widthR
        val provisional = facePath(config.shape, config.pillOrientation, config.fit, wf, hf)
        val provisionalOutline = FaceOutline(provisional, wf / 2f, hf / 2f)
        val strokePx = strokeR * provisionalOutline.rMin
        val path = if (strokePx > 0f) {
            facePath(
                config.shape, config.pillOrientation, config.fit,
                wf - strokePx, hf - strokePx,
            ).also { it.offset(strokePx / 2f, strokePx / 2f) }
        } else {
            provisional
        }

        val outline = FaceOutline(path, wf / 2f, hf / 2f)
        val r = outline.rMin

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        when (config.faceFill) {
            FaceFill.NONE -> Unit
            FaceFill.CONTAINER -> {
                fill.color = colours.face
                canvas.drawPath(path, fill)
            }
        }
        if (strokePx > 0f) {
            canvas.drawPath(
                path,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = strokePx
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                    color = colours.outline
                },
            )
        }

        drawIndices(context, canvas, config, colours, outline, path, r)
        drawNumerals(context, canvas, config, colours, outline, r)

        return Face(bitmap, r, wf / 2f, hf / 2f)
    }

    /** Dots or ticks at the hour positions the numerals do not occupy. */
    private fun drawIndices(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        colours: Colours,
        outline: FaceOutline,
        path: Path,
        r: Float,
    ) {
        if (config.minorIndices == MinorIndex.NONE) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colours.minor }
        val numbered = if (config.numerals == NumeralSystem.NONE) emptySet() else config.indexSet.hours

        when (config.minorIndices) {
            MinorIndex.DOTS, MinorIndex.RINGS -> {
                val filled = config.minorIndices == MinorIndex.DOTS
                paint.style = if (filled) Paint.Style.FILL else Paint.Style.STROKE
                paint.strokeWidth = 0.014f * r
                val radius = DialGeometry.MINOR_DOT_RADIUS * r * (if (filled) 1.0f else 1.35f)
                for (hour in 1..12) {
                    if (hour in numbered) continue
                    val at = ringPoint(outline, config, hour * 30f, r, DialGeometry.NUMERAL_RING)
                    canvas.drawCircle(at.first, at.second, radius, paint)
                }
            }
            MinorIndex.TICKS, MinorIndex.TICKS_60 -> {
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                val count = if (config.minorIndices == MinorIndex.TICKS_60) 60 else 12
                for (i in 0 until count) {
                    val isHour = i % (count / 12) == 0
                    val hour = i / (count / 12) + if (i % (count / 12) == 0) 0 else 0
                    if (isHour && (hour.takeIf { it != 0 } ?: 12) in numbered) continue
                    val angle = i * 360f / count
                    val len = (if (isHour) DialGeometry.TICK_LENGTH else DialGeometry.TICK_LENGTH_MINOR) * r
                    paint.strokeWidth = (if (isHour) 0.014f else 0.008f) * r
                    val outer = ringPoint(outline, config, angle, r, 1f, inset = 0.04f * r)
                    val inner = ringPoint(outline, config, angle, r, 1f, inset = 0.04f * r + len)
                    canvas.drawLine(outer.first, outer.second, inner.first, inner.second, paint)
                }
            }
            MinorIndex.NONE -> Unit
        }
    }

    private fun drawNumerals(
        context: Context,
        canvas: Canvas,
        config: WidgetConfig,
        colours: Colours,
        outline: FaceOutline,
        r: Float,
    ) {
        if (config.numerals == NumeralSystem.NONE || config.indexSet == IndexSet.NONE) return

        val ringR = maxOf(
            DialGeometry.NUMERAL_RING,
            if (config.indexSet == IndexSet.ALL_TWELVE) DialGeometry.MIN_RING_FOR_TWELVE else 0f,
        )

        val widthVal = if (config.analogFont == WidgetFontFamily.GOOGLE_SANS) 25f else 100f

        val probe = TextInk.numeralPaint(
            context, 100f,
            width = widthVal,
            weight = config.analogWeight.weightInt,
            roundness = config.numeralRound.axis,
            system = config.numerals,
            fontFamily = config.analogFont,
        )
        val widest = TextInk.widest(config.numerals)
        val probeInk = TextInk.ink(probe, widest)
        val widestPerCap = if (probeInk.height() > 0) {
            probeInk.width().toFloat() / probeInk.height().toFloat()
        } else {
            1f
        }
        val capR = DialGeometry.clampedCap(config.numeralSize, config.indexSet, ringR, widestPerCap)
        val capPx = capR * r

        val paint = TextInk.numeralPaint(
            context, 100f,
            width = widthVal,
            weight = config.analogWeight.weightInt,
            roundness = config.numeralRound.axis,
            system = config.numerals,
            fontFamily = config.analogFont,
        ).apply {
            color = colours.onFace
            textSize = TextInk.sizeForCap(this, widest, capPx)
        }
        val ink = Rect()

        for (hour in config.indexSet.hours) {
            val text = TextInk.label(hour, config.numerals)
            if (text.isEmpty()) continue
            TextInk.ink(paint, text, ink)
            val angleDeg = (hour % 12) * 30f
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()

            val (x, y, rotation) = when (config.numeralLayout) {
                NumeralLayout.CIRCLE_UPRIGHT -> {
                    val rr = ringR * r
                    Triple(
                        outline.cx + sin(angleRad) * rr,
                        outline.cy - cos(angleRad) * rr,
                        0f,
                    )
                }
                NumeralLayout.SHAPE_UPRIGHT, NumeralLayout.SHAPE_ROTATED -> {
                    val margin = 0.10f * r
                    val hit = outline.placeGlyph(angleRad, ink, margin)
                    val rot = if (config.numeralLayout == NumeralLayout.SHAPE_ROTATED) {
                        angleDeg + if (angleDeg.mod(360f) in 90f..270f) 180f else 0f
                    } else {
                        0f
                    }
                    if (hit == null) {
                        Triple(outline.cx, outline.cy, 0f)
                    } else {
                        Triple(hit.x, hit.y, rot)
                    }
                }
            }

            canvas.save()
            if (rotation != 0f) canvas.rotate(rotation, x, y)
            canvas.drawText(
                text,
                x - ink.width() / 2f - ink.left,
                y + ink.height() / 2f - ink.bottom,
                paint,
            )
            canvas.restore()
        }
    }

    /** A point at [angleDeg] on the clock face dial. */
    private fun ringPoint(
        outline: FaceOutline,
        config: WidgetConfig,
        angleDeg: Float,
        r: Float,
        ringFraction: Float,
        inset: Float = 0f,
    ): Pair<Float, Float> {
        val a = Math.toRadians(angleDeg.toDouble()).toFloat()
        val rr = ringFraction * r - inset
        return outline.cx + sin(a) * rr to outline.cy - cos(a) * rr
    }
}
