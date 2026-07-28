@file:OptIn(ExperimentalJsExport::class)

package logic.tools.markdown.komponents.markdown

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.asList
import kotlin.js.jsTypeOf

/** Placeholder inserted into an otherwise-empty element so it stays visible/selectable in the DOM. */
val ZERO_WIDTH_SPACE: String = 0x200B.toChar().toString()

/** Implemented by consumer-defined custom elements that should round-trip through a document's `getMarkdown()`. */
@JsExport
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

/**
 * Structural stand-in for `node as? MarkdownElement`. A plain `is MarkdownElement` check only
 * recognizes classes the Kotlin compiler itself generated — `@JsExport`ed interfaces carry a hidden
 * marker stamped on instances at compile time, which a consumer's own JS/TS class (implementing the
 * same `getMarkdown()` shape without going through this compiler) can never have. Consumer-defined
 * custom elements only need to expose a callable `getMarkdown`, so duck-type on that instead.
 */
fun asMarkdownElement(node: Any?): MarkdownElement? =
    if (node != null && jsTypeOf(node.asDynamic().getMarkdown) == "function") node.unsafeCast<MarkdownElement>() else null

fun Node.getMarkdownWithTextForElement(): String =
    childNodes.asList().joinToString("") { child ->
        asMarkdownElement(child)?.getMarkdown() ?: child.textContent ?: ""
    }
