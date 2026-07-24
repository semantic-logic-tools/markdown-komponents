package logic.tools.markdown.komponents.markdown

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.asList

/** Placeholder inserted into an otherwise-empty element so it stays visible/selectable in the DOM. */
val ZERO_WIDTH_SPACE: String = 0x200B.toChar().toString()

interface MarkdownElement {
    fun getMarkdown(): String

    /** True for elements that cannot live inside other blocks, but must be a direct child of markdown-document. */
    val mustBeDirectChildOfDocument: Boolean
}

/** Lists have levels, quotes have levels: Tab/Shift-Tab nests/unnests content. */
interface MarkdownElementWithLevel {
    fun goDownOneLevel(child: Element?)
    fun goUpOneLevel(child: Element?)
}

/** An element content can escape out of via Backspace at its start (e.g. a quote). */
interface MarkdownElementEscapeByBackspace {
    fun escapeByBackspace(child: Element?)
}

fun Node.getMarkdownWithTextForElement(): String =
    childNodes.asList().joinToString("") { child ->
        if (child is MarkdownElement) child.getMarkdown() else child.textContent ?: ""
    }
