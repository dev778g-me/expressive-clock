package com.dev.expressiveclock.widget

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import com.dev.expressiveclock.R
import com.dev.expressiveclock.data.NumeralSystem
import com.dev.expressiveclock.data.WidgetFontFamily

object TextInk {

    fun numeralPaint(
        context: Context,
        sizePx: Float,
        width: Float = 100f,
        weight: Int = 500,
        roundness: Float = 0f,
        system: NumeralSystem = NumeralSystem.ARABIC,
        fontFamily: WidgetFontFamily = WidgetFontFamily.GOOGLE_SANS,
    ): Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        typeface = when {
            system == NumeralSystem.EASTERN_ARABIC -> ResourcesCompat.getFont(context, R.font.noto_sans_arabic_digits)
            fontFamily == WidgetFontFamily.GOOGLE_SANS || fontFamily == WidgetFontFamily.GOOGLE_SANS_NORMAL -> ResourcesCompat.getFont(context, R.font.google_sans_flex)
            fontFamily == WidgetFontFamily.SERIF -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Typeface.create(Typeface.SERIF, weight, false)
            } else {
                Typeface.create(Typeface.SERIF, if (weight >= 600) Typeface.BOLD else Typeface.NORMAL)
            }
            fontFamily == WidgetFontFamily.MONOSPACE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Typeface.create(Typeface.MONOSPACE, weight, false)
            } else {
                Typeface.create(Typeface.MONOSPACE, if (weight >= 600) Typeface.BOLD else Typeface.NORMAL)
            }
            fontFamily == WidgetFontFamily.CURSIVE -> Typeface.create("cursive", Typeface.NORMAL)
            else -> Typeface.SANS_SERIF
        }
        textSize = sizePx
        textAlign = Paint.Align.LEFT
        if (system == NumeralSystem.EASTERN_ARABIC) {
            setFontVariationSettings("'wght' $weight")
        } else {
            fontFeatureSettings = "tnum, lnum"
            setFontVariationSettings(
                "'opsz' 144,'wdth' $width,'wght' $weight,'GRAD' 0,'ROND' $roundness"
            )
        }
    }

    fun sizeForCap(paint: Paint, sample: String, capPx: Float): Float {
        val r = Rect()
        paint.textSize = 100f
        paint.getTextBounds(sample, 0, sample.length, r)
        val inkAt100 = r.height().toFloat().takeIf { it > 0f } ?: 72.8f
        return 100f * capPx / inkAt100
    }

    /** Ink bounds of [text] at the paint's current size, the box the glyphs actually occupy. */
    fun ink(paint: Paint, text: String, out: Rect = Rect()): Rect {
        paint.getTextBounds(text, 0, text.length, out)
        return out
    }

    fun label(hour: Int, system: NumeralSystem): String {
        val h = ((hour - 1).mod(12)) + 1
        return when (system) {
            NumeralSystem.NONE -> ""
            NumeralSystem.ARABIC -> h.toString()
            NumeralSystem.ROMAN -> ROMAN[h - 1]
            NumeralSystem.EASTERN_ARABIC -> h.toString().map { EASTERN[it - '0'] }.joinToString("")
        }
    }

    /** The widest string the system will ever put on a dial. The collision clamp measures it. */
    fun widest(system: NumeralSystem): String = when (system) {
        NumeralSystem.NONE -> ""
        NumeralSystem.ARABIC -> "12"
        NumeralSystem.ROMAN -> "VIII"
        NumeralSystem.EASTERN_ARABIC -> "١٢"
    }

    private val ROMAN = arrayOf(
        "I", "II", "III", "IIII", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII",
    )

    /** U+0660 ARABIC-INDIC DIGIT ZERO … NINE. */
    private val EASTERN = CharArray(10) { (0x0660 + it).toChar() }
}
