package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.define
import kotlinx.browser.window

fun registerMarkdownComponents() {
    window.customElements.define("markdown-paragraph", MarkdownParagraph::class.js)
    window.customElements.define("markdown-break", MarkdownBreak::class.js)
    window.customElements.define("markdown-header-1", MarkdownHeading1::class.js)
    window.customElements.define("markdown-header-2", MarkdownHeading2::class.js)
    window.customElements.define("markdown-header-3", MarkdownHeading3::class.js)
    window.customElements.define("markdown-header-4", MarkdownHeading4::class.js)
    window.customElements.define("markdown-header-5", MarkdownHeading5::class.js)
    window.customElements.define("markdown-header-6", MarkdownHeading6::class.js)
    window.customElements.define("markdown-strong", MarkdownStrong::class.js)
    window.customElements.define("markdown-emphasis", MarkdownEmphasis::class.js)
    window.customElements.define("markdown-strike", MarkdownStrike::class.js)
    window.customElements.define("markdown-code-span", MarkdownCodeSpan::class.js)
    window.customElements.define("markdown-html", MarkdownHtml::class.js)
    window.customElements.define("markdown-quote", MarkdownQuote::class.js)

    window.customElements.define("markdown-list", MarkdownList::class.js)
    window.customElements.define("markdown-list-item", MarkdownListItem::class.js)
    window.customElements.define("markdown-numeric-list", MarkdownNumericList::class.js)
    window.customElements.define("markdown-numeric-list-item", MarkdownNumericListItem::class.js)
    window.customElements.define("markdown-task-list-item", MarkdownTaskListItem::class.js, arrayOf("checked"))
    window.customElements.define("markdown-table", MarkdownTable::class.js)
    window.customElements.define("markdown-table-row", MarkdownTableRow::class.js)
    window.customElements.define("markdown-table-header-row", MarkdownTableHeaderRow::class.js)
    window.customElements.define("markdown-table-cell", MarkdownTableCell::class.js)
    window.customElements.define("markdown-table-header-cell", MarkdownTableHeaderCell::class.js)

    window.customElements.define("markdown-link", MarkdownLink::class.js, arrayOf("destination", "title"))
    window.customElements.define("markdown-image", MarkdownImage::class.js, arrayOf("destination", "title"))
    window.customElements.define("markdown-code", MarkdownCode::class.js)

    window.customElements.define("markdown-toc", MarkdownToc::class.js)
}
