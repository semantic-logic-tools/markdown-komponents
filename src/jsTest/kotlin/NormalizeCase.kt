import markdown.ZERO_WIDTH_SPACE

/**
 * A before/after DOM fixture for the normalize regression suite.
 *
 * To capture a new one from a live bug: in devtools, set
 * `document.querySelector('markdown-document').autoNormalize = false`, reproduce the broken
 * shape, then `copy(el.innerHTML)` for [before]. Call `el.normalizeContent()` manually and
 * `copy(el.innerHTML)` again once the fix makes it produce the right thing, for [after].
 */
data class NormalizeCase(
    val name: String,
    val before: String,
    val after: String,
    val containerTag: String = "markdown-document",
)

val normalizeCases = listOf(
    NormalizeCase(
        name = "empty document gets a trailing empty paragraph",
        before = "",
        after = "<markdown-paragraph>$ZERO_WIDTH_SPACE</markdown-paragraph>",
    ),
    NormalizeCase(
        name = "a lone <br> in a paragraph becomes a zero-width space",
        containerTag = "markdown-paragraph",
        before = "<br>",
        after = ZERO_WIDTH_SPACE,
    ),
    NormalizeCase(
        name = "a double <br><br> in a paragraph splits it into two paragraphs",
        before = "<markdown-paragraph><br><br></markdown-paragraph>",
        after = "<markdown-paragraph>$ZERO_WIDTH_SPACE</markdown-paragraph>" +
            "<markdown-paragraph>$ZERO_WIDTH_SPACE</markdown-paragraph>",
    ),
    NormalizeCase(
        name = "a childless <markdown-break> is not removed as if it were an empty leaf",
        before = "<markdown-paragraph>before</markdown-paragraph><markdown-break></markdown-break><markdown-paragraph>after</markdown-paragraph>",
        after = "<markdown-paragraph>before</markdown-paragraph><markdown-break></markdown-break><markdown-paragraph>after</markdown-paragraph>",
    ),
)
