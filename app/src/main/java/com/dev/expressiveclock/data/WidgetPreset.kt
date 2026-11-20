package com.dev.expressiveclock.data

enum class WidgetPreset(val label: String, val config: WidgetConfig) {

    /** The default: a filled circle, four numerals, dots between them. */
    CLASSIC(
        "Classic",
        WidgetConfig(),
    ),

    /** No container at all: a hairline ring, two needles, and a disc for the seconds. */
    BARE(
        "Bare",
        WidgetConfig(
            faceFill = FaceFill.NONE,
            outline = OutlineStyle.THIN,
            hourHand = HandStyle.NEEDLE,
            minuteHand = HandStyle.NEEDLE,
            secondHand = HandStyle.DOT,
            numerals = NumeralSystem.NONE,
            indexSet = IndexSet.NONE,
            minorIndices = MinorIndex.NONE,
        ),
    ),

    /** Twelve Roman numerals turned with the outline, the way a dial has always set them. */
    ROMAN(
        "Roman",
        WidgetConfig(
            shape = FaceShape.SQUIRCLE,
            hourHand = HandStyle.TAPER,
            minuteHand = HandStyle.TAPER,
            numerals = NumeralSystem.ROMAN,
            indexSet = IndexSet.ALL_TWELVE,
            minorIndices = MinorIndex.NONE,
            numeralSize = NumeralSize.SMALL,
            numeralLayout = NumeralLayout.SHAPE_UPRIGHT,
            numeralWeight = NumeralWeight.LIGHT,
        ),
    ),

    /** Sixty ticks and arrowheads: no numerals, and none needed. */
    RAILWAY(
        "Railway",
        WidgetConfig(
            shape = FaceShape.ROUNDED_SQUARE,
            hourHand = HandStyle.ARROW,
            minuteHand = HandStyle.ARROW,
            secondHand = HandStyle.BALL,
            numerals = NumeralSystem.NONE,
            indexSet = IndexSet.NONE,
            minorIndices = MinorIndex.TICKS_60,
        ),
    ),

    /** The expressive one: a flower outline, open rings for the hours, leaf hands. */
    BLOOM(
        "Bloom",
        WidgetConfig(
            shape = FaceShape.FLOWER,
            hourHand = HandStyle.LEAF,
            minuteHand = HandStyle.LEAF,
            numerals = NumeralSystem.NONE,
            indexSet = IndexSet.NONE,
            minorIndices = MinorIndex.RINGS,
        ),
    ),
    ;

    companion object {
        /** The preset [config] is exactly, or null when the configuration has been hand-edited. */
        fun matching(config: WidgetConfig): WidgetPreset? = entries.firstOrNull { it.config == config }
    }
}
