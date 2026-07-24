package logic.tools.markdown.komponents

import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.Range

/**
 * `Selection`/`Document.getSelection()` aren't part of kotlin-dom-api-compat. This is the minimal
 * typed surface the editor needs; the single `asDynamic()` is localized here instead of leaking
 * into every call site.
 */
external interface Selection {
    val anchorNode: Node?
    val anchorOffset: Int
    val focusNode: Node?
    val focusOffset: Int
    val isCollapsed: Boolean
    val rangeCount: Int
    fun getRangeAt(index: Int): Range
    fun addRange(range: Range)
    fun removeAllRanges()
    fun collapseToEnd()
    fun collapseToStart()
    fun containsNode(node: Node, allowPartialContainment: Boolean = definedExternally): Boolean
    fun deleteFromDocument()
}

fun Document.getSelection(): Selection? = asDynamic().getSelection()

fun currentSelection(): Selection? = document.getSelection()

/** WebKit-only fallback for [Document.caretPositionFromPoint]; not in kotlin-dom-api-compat. */
fun Document.caretRangeFromPoint(x: Double, y: Double): Range? = asDynamic().caretRangeFromPoint(x, y)
