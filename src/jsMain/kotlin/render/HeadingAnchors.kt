package render

/**
 * This library's markdown convention for attaching an explicit id to a heading or fenced code
 * block: a trailing `{explicit-id}` annotation, e.g. `## Title {my-id}`. Any parser adapter that
 * wants to support heading/code ids can use this to extract one from the raw text/meta string the
 * underlying parsing engine hands it.
 */
object HeadingAnchors {
    private val anchorRegex = Regex("\\s*\\{([^}]+)\\}$")
    private val slugRegex = Regex("[^\\wа-яіїє]+", RegexOption.IGNORE_CASE)

    /**
     * Returns (id, remainingText). If [text] has no trailing `{id}` annotation, falls back to a
     * slug of [text] itself when [useTextInId] is true (headings), or a random id otherwise (code
     * blocks, where the text isn't a meaningful id source).
     */
    fun extract(idPrefixForDefault: String, text: String, useTextInId: Boolean): Pair<String, String> {
        val match = anchorRegex.find(text)
        return if (match != null) {
            match.groupValues[1] to text.substring(0, match.range.first)
        } else {
            val id = if (useTextInId) {
                idPrefixForDefault + slugify(text)
            } else {
                idPrefixForDefault + (kotlin.js.Date().getTime() + kotlin.random.Random.nextDouble())
            }
            id to text
        }
    }

    fun slugify(text: String): String = text.lowercase().replace(slugRegex, "-")
}
