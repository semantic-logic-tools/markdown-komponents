@file:OptIn(ExperimentalJsExport::class)

package logic.tools.markdown.komponents.markdown

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.asList
import logic.tools.markdown.komponents.BaseWebComponent
import logic.tools.markdown.komponents.Selection

@JsExport
abstract class MarkdownComponent : BaseWebComponent(), MarkdownElement {

    /** If true, this element may contain text nodes as children that represent user content. */
    abstract fun containsMarkdownTextContent(): Boolean

    open fun isEditable(): Boolean = true

    open fun isDeletableAsAWhole(): Boolean = false

    /** Returns true if normalizing changed something that impacts an ancestor, so it should normalize again too. */
    open fun normalizeContent(): Boolean = normalizeContent(this)

    fun pushNodesAfterBreakToParent(content: HTMLBRElement) {
        val parent = parentNode ?: return
        val siblings = childNodes.asList()
        val indexOfBreak = siblings.indexOf(content)
        val elementsToMove = siblings.drop(indexOfBreak + 1)

        val rightElement: Element? = when {
            elementsToMove.isEmpty() -> newEmptyElementAfterBreak()
            elementsToMove.size == 1 && elementsToMove[0].let { it is Text && it.textContent == ZERO_WIDTH_SPACE } ->
                newEmptyElementNameAfterBreak()?.let { document.createElement(it) }
            else -> document.createElement(tagName)
        }

        if (rightElement != null) {
            elementsToMove.forEach { rightElement.append(it) }
            parent.insertBefore(rightElement, nextSibling)
        } else {
            elementsToMove.forEach { parent.insertBefore(it, nextSibling) }
        }
    }

    fun newEmptyElement(tagName: String): Element {
        val result = document.createElement(tagName)
        if (result is MarkdownComponent) {
            result.fillEmptyElement()
        }
        return result
    }

    fun fillEmptyElement() {
        appendChild(document.createTextNode(ZERO_WIDTH_SPACE)) // stay visible/selectable when empty
    }

    fun newEmptyElementAfterBreak(): Element? =
        newEmptyElementNameAfterBreak()?.let { newEmptyElement(it) }

    open fun newEmptyElementNameAfterBreak(): String? = tagName

    fun pushBreakAndNodesAfterToParent(content: HTMLBRElement) {
        pushNodesAfterBreakToParent(content)
        parentNode?.insertBefore(content, nextSibling)
    }

    open fun mergeWithPrevious(currentSelection: Selection?) {}

    open fun mergeNextIn() {}

    fun setSelectionToEnd(currentSelection: Selection?) {
        var last: Node = this
        while (last.lastChild != null) {
            last = last.lastChild!!
        }
        if (last is Text) {
            val range = document.createRange()
            range.setStart(last, last.length)
            range.collapse(true)
            currentSelection?.removeAllRanges()
            currentSelection?.addRange(range)
        }
    }

    open fun contentLength(): Int {
        var result = 0
        childNodes.asList().forEach { child ->
            result += when {
                child is MarkdownComponent -> child.contentLength()
                child is HTMLBRElement -> 1
                else -> child.textContent?.replace(ZERO_WIDTH_SPACE, "")?.length ?: 0
            }
        }
        return result + endOfLineEquivalentLength()
    }

    open fun contentLengthUntil(child: Node?): Int {
        val nodes = childNodes.asList()
        val indexOfChild = if (child == null) -1 else nodes.indexOf(child)
        var result = 0
        if (indexOfChild >= 0) {
            nodes.subList(0, indexOfChild).forEach { sibling ->
                result += when {
                    sibling is MarkdownComponent -> sibling.contentLength()
                    sibling is HTMLBRElement -> 1
                    else -> sibling.textContent?.replace(ZERO_WIDTH_SPACE, "")?.length ?: 0
                }
            }
        }
        return result
    }

    open fun elementEndWithEndOfLineEquivalent(): Boolean = false

    fun endOfLineEquivalentLength(): Int = if (elementEndWithEndOfLineEquivalent()) 1 else 0

    fun getNodeAndOffsetFromContentOffsetAnchor(contentOffset: Int): Pair<Node, Int> {
        val nodes = childNodes.asList()
        if (nodes.isEmpty()) return this to contentOffset

        var resultNode: Node = nodes[0]
        for (child in nodes) {
            val lengthUntilChild = contentLengthUntil(child)
            if (contentOffset == lengthUntilChild) {
                resultNode = child
                break
            }
            if (contentOffset < lengthUntilChild) break
            resultNode = child
        }
        return if (resultNode is MarkdownComponent) {
            resultNode.getNodeAndOffsetFromContentOffsetAnchor(contentOffset - contentLengthUntil(resultNode))
        } else {
            resultNode to (contentOffset - contentLengthUntil(resultNode))
        }
    }

    fun getNodeAndOffsetFromContentOffset(contentOffset: Int): Pair<Node, Int> {
        val nodes = childNodes.asList()
        if (nodes.isEmpty()) return this to contentOffset

        var resultNode: Node = nodes[0]
        var previousNodeWasEol = false
        for (child in nodes) {
            val lengthUntilChild = contentLengthUntil(child)
            if (contentOffset == lengthUntilChild) {
                if (previousNodeWasEol) resultNode = child
                break
            }
            if (contentOffset < lengthUntilChild) break
            resultNode = child
            previousNodeWasEol = child is MarkdownComponent && child.elementEndWithEndOfLineEquivalent()
        }
        return if (resultNode is MarkdownComponent) {
            resultNode.getNodeAndOffsetFromContentOffset(contentOffset - contentLengthUntil(resultNode))
        } else {
            resultNode to (contentOffset - contentLengthUntil(resultNode))
        }
    }

    override fun getMarkdown(): String =
        children.asList().joinToString("") { child -> asMarkdownElement(child)?.getMarkdown() ?: "" }
}
