package logic.tools.markdown.komponents.render

/**
 * Produces the `<markdown-*>` custom-element markup for each markdown construct this library's
 * components understand. Deliberately independent of any particular markdown parsing engine — an
 * adapter for a specific engine (e.g. the optional `tsstack` package, which drives one of these
 * from `@ts-stack/markdown`) calls into an implementation as it walks whatever tokens that engine
 * produces. [MarkdownComponentsRenderer] is the default/reference implementation.
 */
interface MarkdownRenderer {
    fun heading(text: String, level: Int, id: String?): String
    fun paragraph(text: String): String
    fun blockquote(quote: String): String
    fun html(html: String): String
    fun hr(): String
    fun list(body: String, ordered: Boolean): String
    fun listItem(text: String): String
    fun taskListItem(text: String, checked: Boolean): String
    fun table(header: String, body: String): String
    fun tableRow(content: String): String
    fun tableHeaderRow(content: String): String
    fun tableCell(content: String): String
    fun tableHeaderCell(content: String): String
    fun code(code: String, lang: String?, id: String?): String
    fun codeSpan(text: String): String
    fun strong(text: String): String
    fun emphasis(text: String): String
    fun strikethrough(text: String): String
    fun link(href: String, title: String?, text: String): String
    fun image(href: String, title: String?, text: String): String
    /** Referenced by the default [MarkdownComponentsRenderer.paragraph] to expand a literal `${toc}` placeholder. */
    fun toc(): String
    /** Renders an arbitrary custom-tagged block, e.g. this library's `[[[tag ... tag]]]` extension. */
    fun custom(tag: String, content: String): String
}
