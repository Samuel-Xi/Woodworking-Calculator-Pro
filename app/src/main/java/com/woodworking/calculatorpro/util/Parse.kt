package com.woodworking.calculatorpro.util

/**
 * Forgiving number parsers used by the live-input fields. Returns null when
 * the user hasn't typed anything sensible yet; callers treat null as "do not
 * compute" rather than as zero.
 *
 * The parser first tries a plain decimal (the common case), then delegates
 * to [Fraction] for mixed numbers and feet/inch notation. This lets every
 * existing input field accept "3 1/8", "5' 6 3/4\"" etc. without any
 * per-screen changes.
 *
 * When the user has typed feet/inch markers, the returned Double is always
 * expressed in **inches** regardless of the field's current display unit. The
 * caller is responsible for converting from inches to its working unit — see
 * [parseLengthInUnit] for the helper that does this correctly.
 */
fun String.parseDoubleOrNull(): Double? = Fraction.parse(this)?.value

fun String.parseIntOrNull(): Int? {
    if (isBlank()) return null
    return trim().toIntOrNull()
}

/**
 * Length-aware parser. If the user typed feet/inch markers (e.g. "5' 6\""),
 * the value is interpreted as inches and converted to [unit]. Otherwise the
 * raw number is returned in [unit] as-is.
 *
 * This is the safest way to consume a free-form length field — it lets a
 * woodworker type "1/2" in an "in" field and "12.5" in a "mm" field without
 * ever surprising them.
 */
fun String.parseLengthInUnit(
    unit: com.woodworking.calculatorpro.domain.LengthUnit,
): Double? {
    val parsed = Fraction.parse(this) ?: return null
    return if (parsed.isImperial) {
        com.woodworking.calculatorpro.domain.convertLength(
            value = parsed.value,
            from = com.woodworking.calculatorpro.domain.LengthUnit.IN,
            to = unit,
        )
    } else {
        parsed.value
    }
}
