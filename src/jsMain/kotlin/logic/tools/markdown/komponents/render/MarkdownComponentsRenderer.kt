@file:OptIn(ExperimentalJsExport::class)

package logic.tools.markdown.komponents.render

/** The library's own default [MarkdownRenderer]: renders straight to this library's `<markdown-*>` custom elements. */
@JsExport
open class MarkdownComponentsRenderer : MarkdownRenderer {

    override fun heading(text: String, level: Int, id: String?): String {
        val idAttr = if (!id.isNullOrEmpty()) " id='$id'" else ""
        return "<markdown-header-$level$idAttr>$text</markdown-header-$level>"
    }

    override fun paragraph(text: String): String {
        val replaced = text.replace("\${toc}", toc())
        return "<markdown-paragraph>$replaced</markdown-paragraph>"
    }

    override fun blockquote(quote: String): String = "<markdown-quote>$quote</markdown-quote>"

    override fun html(html: String): String = "<markdown-html>$html</markdown-html>"

    override fun hr(): String = "<markdown-break></markdown-break>"

    override fun list(body: String, ordered: Boolean): String = "<markdown-list ordered='$ordered'>$body</markdown-list>"

    override fun listItem(text: String): String = "<markdown-list-item>$text</markdown-list-item>"

    override fun taskListItem(text: String, checked: Boolean): String {
        val checkedAttr = if (checked) " checked='true'" else ""
        return "<markdown-task-list-item$checkedAttr>$text</markdown-task-list-item>"
    }

    override fun table(header: String, body: String): String = "<markdown-table>$header$body</markdown-table>"

    override fun tableRow(content: String): String = "<markdown-table-row>$content</markdown-table-row>"

    override fun tableHeaderRow(content: String): String = "<markdown-table-header-row>$content</markdown-table-header-row>"

    override fun tableCell(content: String): String = "<markdown-table-cell>$content</markdown-table-cell>"

    override fun tableHeaderCell(content: String): String = "<markdown-table-header-cell>$content</markdown-table-header-cell>"

    override fun code(code: String, lang: String?, id: String?): String {
        val langAttr = if (!lang.isNullOrEmpty()) " lang='$lang'" else ""
        val idAttr = if (!id.isNullOrEmpty()) " id='$id'" else ""
        return "<markdown-code$langAttr$idAttr>$code</markdown-code>"
    }

    override fun codeSpan(text: String): String = "<markdown-code-span>$text</markdown-code-span>"

    override fun strong(text: String): String = "<markdown-strong>$text</markdown-strong>"

    override fun emphasis(text: String): String = "<markdown-emphasis>$text</markdown-emphasis>"

    override fun strikethrough(text: String): String = "<markdown-strike>$text</markdown-strike>"

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

    override fun toc(): String = "<markdown-toc></markdown-toc>"

    override fun custom(tag: String, content: String): String = "<$tag>$content</$tag>"
}
