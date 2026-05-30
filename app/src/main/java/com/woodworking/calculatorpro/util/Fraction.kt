package com.woodworking.calculatorpro.util

/**
 * Imperial fraction parser. Accepts the formats a real woodworker actually
 * types on a phone keyboard, and returns the value as a single Double in the
 * unit indicated by the trailing marker (or a unit-less decimal).
 *
 * Supported forms:
 *   "1/2"                ->  0.5
 *   "3 1/8"              ->  3.125
 *   "3-1/8"              ->  3.125
 *   "5'"                 ->  60.0   (feet -> inches)
 *   "6\""                ->  6.0    (inches)
 *   "5' 6\""             ->  66.0
 *   "5' 6 1/2\""         ->  66.5
 *   "5'6-1/2\""          ->  66.5   (compact form, no spaces)
 *   "1.5"                ->  1.5
 *   "1,5"                ->  1.5    (European decimal comma)
 *   ""  / "  "           ->  null
 *
 * The returned value is in the *inferred* unit:
 *   - "<n>'..." forms always resolve in inches (1 ft = 12 in).
 *   - "<n>\"" forms resolve in inches.
 *   - everything else resolves in the same unit the caller used to write the
 *     number (e.g. "3 1/8" with a field labelled "mm" stays in mm).
 *
 * This is intentionally a pure function with zero Android imports so it can
 * be unit-tested against a long table of real-world inputs.
 */
object Fraction {

    /** Result of a successful parse: the numeric value, and whether feet/inch
     *  markers were detected (so callers can decide how to label the value). */
    data class Parsed(
        val value: Double,
        /** True when the user typed a ' or " — meaning the value is in inches. */
        val isImperial: Boolean,
    )

    /**
     * Try to parse [raw] as either a plain decimal or an imperial fraction.
     * Returns null when the input is empty or doesn't match any supported form.
     */
    fun parse(raw: String): Parsed? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        // Normalise: collapse internal whitespace, unify the feet/inch marks,
        // accept European comma decimals.
        val normalised = trimmed
            .replace('，', ',')
            .replace('、', ' ')
            .replace('’', '\'')
            .replace('′', '\'')
            .replace('“', '"')
            .replace('”', '"')
            .replace('″', '"')
            .replace(',', '.')
            .replace(Regex("\\s+"), " ")

        // Fast path: plain decimal.
        val asPlain = normalised.toDoubleOrNull()
        if (asPlain != null) return Parsed(asPlain, isImperial = false)

        return parseImperial(normalised)
    }

    /** Convenience that mirrors [String.parseDoubleOrNull] for callers that
     *  don't care about the imperial flag. */
    fun parseValue(raw: String): Double? = parse(raw)?.value

    // region implementation ----------------------------------------------------

    private fun parseImperial(s: String): Parsed? {
        var rest = s
        var feet: Double? = null

        // Optional leading feet group: "<num>'"
        val feetMatch = FEET_REGEX.find(rest)
        if (feetMatch != null && feetMatch.range.first == 0) {
            val feetPart = feetMatch.groupValues[1].trim()
            feet = parseFeetOrInchValue(feetPart) ?: return null
            rest = rest.substring(feetMatch.range.last + 1).trim()
        }

        // Strip optional trailing inch mark.
        val hadInchMark = rest.endsWith("\"")
        if (hadInchMark) rest = rest.dropLast(1).trim()

        // The remaining inches portion may be a fraction, a mixed number,
        // empty, or a plain decimal.
        val inches = if (rest.isEmpty()) 0.0
                     else parseFeetOrInchValue(rest) ?: return null

        val isImperial = feet != null || hadInchMark
        val totalInches = (feet ?: 0.0) * 12.0 + inches

        // When neither feet nor inch marks were present, the value isn't
        // imperial — it's just a mixed number in the field's native unit.
        return if (!isImperial) Parsed(totalInches, isImperial = false)
               else Parsed(totalInches, isImperial = true)
    }

    /**
     * Parse a single numeric chunk: integer, decimal, fraction "a/b", or
     * mixed number "n a/b" / "n-a/b".
     */
    private fun parseFeetOrInchValue(raw: String): Double? {
        val token = raw.replace('-', ' ').trim()
        if (token.isEmpty()) return null

        // Mixed number: "n a/b"
        val mixed = MIXED_REGEX.matchEntire(token)
        if (mixed != null) {
            val whole = mixed.groupValues[1].toDoubleOrNull() ?: return null
            val num   = mixed.groupValues[2].toDoubleOrNull() ?: return null
            val den   = mixed.groupValues[3].toDoubleOrNull() ?: return null
            if (den == 0.0) return null
            return whole + num / den
        }

        // Pure fraction: "a/b"
        val frac = FRACTION_REGEX.matchEntire(token)
        if (frac != null) {
            val num = frac.groupValues[1].toDoubleOrNull() ?: return null
            val den = frac.groupValues[2].toDoubleOrNull() ?: return null
            if (den == 0.0) return null
            return num / den
        }

        return token.toDoubleOrNull()
    }

    // We greedily eat everything up to the first apostrophe as the feet
    // portion — it's allowed to contain a decimal or a fraction, but not
    // another apostrophe.
    private val FEET_REGEX  = Regex("""^([^']+)'""")
    private val MIXED_REGEX = Regex("""(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)/(\d+(?:\.\d+)?)""")
    private val FRACTION_REGEX = Regex("""(\d+(?:\.\d+)?)/(\d+(?:\.\d+)?)""")

    // endregion
}
