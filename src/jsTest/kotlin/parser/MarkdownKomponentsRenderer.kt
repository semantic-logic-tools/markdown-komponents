package parser

/*
  Ported from the original markdown-editable-components' marked-renderer.ts — see that file's
  header comment for the background links (heading-id syntax, mdast code nodes, cross-referencing
  named anchors, and the ts-stack/markdown renderer override API).
*/

/** Renders parsed markdown into the `<markdown-*>` custom-element tags this library's components register. */
open class MarkdownKomponentsRenderer : TsStackMarkdown.Renderer() {

    fun toc(): String = "<markdown-toc></markdown-toc>"

    override fun code(code: String, lang: String?, escaped: Boolean?, meta: String?): String {
        var codeText = code
        var isEscaped = escaped
        if (isEscaped != true) {
            options.escape?.let { escape ->
                codeText = escape(codeText, false)
                isEscaped = true
            }
        }
        return if (!meta.isNullOrEmpty()) {
            val (id, newLang) = parseAnchor("code-", meta, false)
            codeWithAnchor(codeText, newLang, id, isEscaped)
        } else {
            codeWithAnchor(codeText, lang, null, isEscaped)
        }
    }

    fun codeWithAnchor(code: String, lang: String?, id: String?, escaped: Boolean?): String {
        var codeText = code
        var isEscaped = escaped
        val langAttr = if (!lang.isNullOrEmpty()) "lang='$lang'" else ""
        val idAttr = if (!id.isNullOrEmpty()) "id='$id'" else ""
        if (isEscaped != true) {
            options.escape?.let { escape ->
                codeText = escape(codeText, false)
                isEscaped = true
            }
        }
        return "<markdown-code $langAttr $idAttr>$codeText</markdown-code>"
    }

    fun custom(tag: String, content: String): String = "<$tag>$content</$tag>"

    override fun blockquote(quote: String): String = "<markdown-quote>$quote</markdown-quote>"

    override fun html(html: String): String = "<markdown-html>$html</markdown-html>"

    override fun heading(text: String, level: Int, raw: String): String {
        val (id, newText) = parseAnchor("heading-", text, true)
        return headingWithAnchor(newText, level, raw, id)
    }

    fun headingWithAnchor(text: String, level: Int, raw: String, id: String?): String {
        val idAttr = if (!id.isNullOrEmpty()) "id='$id'" else ""
        return "<markdown-header-$level $idAttr>$text</markdown-header-$level>"
    }

    override fun list(body: String, ordered: Boolean?): String = "<markdown-list ordered='$ordered'>$body</markdown-list>"

    override fun listitem(text: String): String = when {
        text.startsWith("<markdown-paragraph>[ ] ") ->
            // see https://github.com/ts-stack/markdown/issues/8
            "<markdown-task-list-item>${text.replacePrefixOnce("<markdown-paragraph>[ ] ", "<markdown-paragraph>")}</markdown-task-list-item>"
        text.startsWith("<markdown-paragraph>[x] ") ->
            "<markdown-task-list-item checked='true'>${text.replacePrefixOnce("<markdown-paragraph>[x] ", "<markdown-paragraph>")}</markdown-task-list-item>"
        text.startsWith("[ ] ") ->
            // see https://github.com/ts-stack/markdown/issues/8
            "<markdown-task-list-item>${text.removePrefix("[ ] ")}</markdown-task-list-item>"
        text.startsWith("[x] ") ->
            "<markdown-task-list-item checked='true'>${text.removePrefix("[x] ")}</markdown-task-list-item>"
        else -> "<markdown-list-item>$text</markdown-list-item>"
    }

    override fun paragraph(text: String): String {
        val replaced = text.replace("\${toc}", toc())
        return "<markdown-paragraph>$replaced</markdown-paragraph>"
    }

    override fun codespan(text: String): String = "<markdown-code-span>$text</markdown-code-span>"

    override fun hr(): String = "<markdown-break></markdown-break>"

    override fun strong(text: String): String = "<markdown-strong>$text</markdown-strong>"

    override fun em(text: String): String = "<markdown-emphasis>$text</markdown-emphasis>"

    override fun del(text: String): String = "<markdown-strike>$text</markdown-strike>"

    override fun table(header: String, body: String): String {
        val headerWithHeaderRows = header.replace("markdown-table-row>", "markdown-table-header-row>")
        return "<markdown-table>$headerWithHeaderRows$body</markdown-table>"
    }

    override fun tablerow(content: String): String = "<markdown-table-row>$content</markdown-table-row>"

    override fun tablecell(content: String, flags: dynamic): String =
        if (flags.header == true) {
            "<markdown-table-header-cell>$content</markdown-table-header-cell>"
        } else {
            "<markdown-table-cell>$content</markdown-table-cell>"
        }

    override fun link(href: String, title: String?, text: String): String =
        if (!title.isNullOrEmpty()) {
            "<markdown-link destination='$href' title='$title'>$text</markdown-link>"
        } else {
            "<markdown-link destination='$href'>$text</markdown-link>"
        }

    override fun image(href: String, title: String?, text: String): String =
        if (!title.isNullOrEmpty()) {
            "<markdown-image destination='$href' title='$title'>$text</markdown-image>"
        } else {
            "<markdown-image destination='$href'>$text</markdown-image>"
        }

    /**
     * Splits a trailing `{explicit-id}` annotation off [text] (used for both heading and
     * fenced-code anchors), falling back to a slugified-text id (headings) or a random one (code
     * blocks without an explicit id).
     */
    fun parseAnchor(idPrefixForDefault: String, text: String, useTextInId: Boolean): Pair<String, String> {
        val match = anchorRegex.find(text)
        return if (match != null) {
            val id = match.groupValues[1]
            id to text.substring(0, match.range.first)
        } else {
            val id = if (useTextInId) {
                idPrefixForDefault + text.lowercase().replace(slugRegex, "-")
            } else {
                idPrefixForDefault + (kotlin.js.Date().getTime() + kotlin.random.Random.nextDouble())
            }
            id to text
        }
    }

    companion object {
        // Kotlin/JS's Regex compiles with the unicode ('u') flag, which (unlike plain JS RegExp)
        // requires the closing brace to be escaped too — an unescaped `}` is a syntax error under 'u'.
        private val anchorRegex = Regex("\\s*\\{([^}]+)\\}$")
        private val slugRegex = Regex("[^\\wа-яіїє]+", RegexOption.IGNORE_CASE)
    }
}

private fun String.replacePrefixOnce(prefix: String, replacement: String): String =
    if (startsWith(prefix)) replacement + substring(prefix.length) else this

private val fencedCodeWithAnchorRule =
    JsRegExp("""^ *(`{3,}|~{3,})[ .]*(\S+)? +\{([^}]+)} *\n([\s\S]*?)\s*\1 *(?:\n+|$)""")

private val customBlockRule =
    JsRegExp("""^\[\[\[([a-z\-]+)\n([\s\S]+?)\1\]\]\]\n""")

/** Converts [markdown] to the HTML string of `<markdown-*>` tags used as a document's `innerHTML`. */
fun parseMarkdown(markdown: String, renderer: MarkdownKomponentsRenderer? = null): String {
    val options = TsStackMarkdown.MarkedOptions()
    options.gfm = true

    val actualRenderer = renderer ?: MarkdownKomponentsRenderer()
    options.renderer = actualRenderer

    // fenced code block with an explicit `{id}` (and optional language), e.g. ```kotlin {my-id}
    TsStackMarkdown.Marked.setBlockRule(fencedCodeWithAnchorRule) { execArr ->
        val groups = execArr!!
        actualRenderer.codeWithAnchor(groups[4]!!, groups[2], groups[3], null)
    }
    // a custom tagged block: [[[tag\n...content...\ntag]]]
    TsStackMarkdown.Marked.setBlockRule(customBlockRule) { execArr ->
        val groups = execArr!!
        actualRenderer.custom(groups[1]!!, parseMarkdown(groups[2]!!, renderer))
    }

    return TsStackMarkdown.Marked.parse(markdown, options)
}
