package tsstack

import render.HeadingAnchors
import render.MarkdownComponentsRenderer
import render.MarkdownRenderer

/**
 * Adapts `@ts-stack/markdown`'s token-driven `Renderer` callback API to this library's
 * [MarkdownRenderer] contract, so any [MarkdownRenderer] — not just [MarkdownComponentsRenderer] —
 * can be driven by ts-stack's parser. Part of this package's optional, ready-made ts-stack
 * integration; nothing elsewhere in the library references it unless you import `tsstack.*`.
 */
open class TsStackMarkdownRenderer(private val delegate: MarkdownRenderer = MarkdownComponentsRenderer()) : TsStackMarkdown.Renderer() {

    // ts-stack passes a per-cell `header` flag but no per-row one — a row is a header row iff any
    // of the cells rendered just before it (via tablecell) were header cells.
    private var currentRowHasHeaderCells = false

    override fun code(code: String, lang: String?, escaped: Boolean?, meta: String?): String =
        if (!meta.isNullOrEmpty()) {
            val (id, newLang) = HeadingAnchors.extract("code-", meta, false)
            codeWithAnchor(code, newLang, id, escaped)
        } else {
            codeWithAnchor(code, lang, null, escaped)
        }

    /** Also called directly by the fenced-code-with-explicit-id block rule registered in [parseMarkdown]. */
    fun codeWithAnchor(code: String, lang: String?, id: String?, escaped: Boolean? = null): String {
        val codeText = if (escaped != true) options.escape?.invoke(code, false) ?: code else code
        return delegate.code(codeText, lang, id)
    }

    override fun blockquote(quote: String): String = delegate.blockquote(quote)

    override fun html(html: String): String = delegate.html(html)

    override fun heading(text: String, level: Int, raw: String): String {
        val (id, newText) = HeadingAnchors.extract("heading-", text, true)
        return delegate.heading(newText, level, id)
    }

    override fun hr(): String = delegate.hr()

    override fun list(body: String, ordered: Boolean?): String = delegate.list(body, ordered == true)

    override fun listitem(text: String): String = when {
        text.startsWith("<markdown-paragraph>[ ] ") ->
            // see https://github.com/ts-stack/markdown/issues/8
            delegate.taskListItem(text.replacePrefixOnce("<markdown-paragraph>[ ] ", "<markdown-paragraph>"), checked = false)
        text.startsWith("<markdown-paragraph>[x] ") ->
            delegate.taskListItem(text.replacePrefixOnce("<markdown-paragraph>[x] ", "<markdown-paragraph>"), checked = true)
        text.startsWith("[ ] ") ->
            delegate.taskListItem(text.removePrefix("[ ] "), checked = false)
        text.startsWith("[x] ") ->
            delegate.taskListItem(text.removePrefix("[x] "), checked = true)
        else -> delegate.listItem(text)
    }

    override fun paragraph(text: String): String = delegate.paragraph(text)

    override fun table(header: String, body: String): String = delegate.table(header, body)

    override fun tablerow(content: String): String {
        val isHeaderRow = currentRowHasHeaderCells
        currentRowHasHeaderCells = false
        return if (isHeaderRow) delegate.tableHeaderRow(content) else delegate.tableRow(content)
    }

    override fun tablecell(content: String, flags: dynamic): String =
        if (flags.header == true) {
            currentRowHasHeaderCells = true
            delegate.tableHeaderCell(content)
        } else {
            delegate.tableCell(content)
        }

    override fun strong(text: String): String = delegate.strong(text)

    override fun em(text: String): String = delegate.emphasis(text)

    override fun codespan(text: String): String = delegate.codeSpan(text)

    override fun del(text: String): String = delegate.strikethrough(text)

    override fun link(href: String, title: String?, text: String): String = delegate.link(href, title, text)

    override fun image(href: String, title: String?, text: String): String = delegate.image(href, title, text)
}

private fun String.replacePrefixOnce(prefix: String, replacement: String): String =
    if (startsWith(prefix)) replacement + substring(prefix.length) else this

private val fencedCodeWithAnchorRule =
    JsRegExp("""^ *(`{3,}|~{3,})[ .]*(\S+)? +\{([^}]+)} *\n([\s\S]*?)\s*\1 *(?:\n+|$)""")

private val customBlockRule =
    JsRegExp("""^\[\[\[([a-z\-]+)\n([\s\S]+?)\1\]\]\]\n""")

/**
 * Parses [markdown] using `@ts-stack/markdown`, driving [renderer] (this library's own components
 * by default) to produce the resulting `<markdown-*>` HTML. Assignable directly to
 * `markdown-document.parser`, e.g. `doc.parser = ::parseMarkdown`.
 */
fun parseMarkdown(markdown: String, renderer: MarkdownRenderer = MarkdownComponentsRenderer()): String {
    val options = TsStackMarkdown.MarkedOptions()
    options.gfm = true

    val tsStackRenderer = TsStackMarkdownRenderer(renderer)
    options.renderer = tsStackRenderer

    // fenced code block with an explicit `{id}` (and optional language), e.g. ```kotlin {my-id}
    TsStackMarkdown.Marked.setBlockRule(fencedCodeWithAnchorRule) { execArr ->
        val groups = execArr!!
        tsStackRenderer.codeWithAnchor(groups[4]!!, groups[2], groups[3])
    }
    // a custom tagged block: [[[tag\n...content...\ntag]]]
    TsStackMarkdown.Marked.setBlockRule(customBlockRule) { execArr ->
        val groups = execArr!!
        renderer.custom(groups[1]!!, parseMarkdown(groups[2]!!, renderer))
    }

    return TsStackMarkdown.Marked.parse(markdown, options)
}
