package logic.tools.markdown.komponents.markdown

import logic.tools.markdown.komponents.Selection
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.asList

/**
 * Shared behavior for `markdown-list-item` and `markdown-numeric-list-item`. The original TS had
 * these as two near-identical, independently-maintained classes (down to duplicated bugs — e.g.
 * only the bullet variant unwrapped a list left with a single item); this consolidates them.
 */
abstract class ListItem : ContainerElement() {
    override val mustBeDirectChildOfDocument = false

    /** How many `markdown-list`/`markdown-numeric-list` ancestors this item is nested under. */
    fun getDepth(): Int {
        var depth = 0
        var ancestor = parentNode
        while (ancestor != null && ancestor.nodeName.lowercase() != "markdown-document") {
            if (ancestor.nodeName.lowercase() == "markdown-list" || ancestor.nodeName.lowercase() == "markdown-numeric-list") {
                depth++
            }
            ancestor = ancestor.parentNode
        }
        return depth
    }

    override fun getMarkdown(): String =
        "  ".repeat(getDepth()) + "- " + getTaskMarkdown() + getMarkdownWithTextForElement()

    open fun getTaskMarkdown(): String = ""

    fun getMarkdownWithTextForElement(): String =
        childNodes.asList().joinToString("") { child ->
            // trim to avoid extra spaces/newlines being interpreted as a paragraph break
            asMarkdownElement(child)?.getMarkdown() ?: (child.textContent?.trim() ?: "") + "\n"
        }

    override fun normalizeContent(): Boolean {
        normalize()
        if (childNodes.length == 0) {
            remove() // an empty leaf serves no purpose
            return true
        }
        childNodes.asList().forEach { content ->
            if (content is HTMLBRElement) {
                pushNodesAfterBreakToParent(content)
                removeChild(content)
                return true
            } else if (content is MarkdownComponent) {
                if (content.normalizeContent()) return normalizeContent()
            } else if (content is Text) {
                val hasZwsp = (content.textContent?.indexOf(ZERO_WIDTH_SPACE) ?: -1) >= 0
                if (content.length > 1 && hasZwsp) {
                    content.textContent = content.textContent?.replace(ZERO_WIDTH_SPACE, "")
                }
            } else if (normalizeUnknownChild(content)) {
                return true
            }
        }
        return false
    }

    override fun contentLength(): Int {
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

    override fun contentLengthUntil(child: Node?): Int {
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

    override fun mergeWithPrevious(currentSelection: Selection?) {
        val previous = previousElementSibling
        if (previous is ListItem) {
            if (currentSelection?.containsNode(this, true) == true) {
                previous.setSelectionToEnd(currentSelection)
            }
            childNodes.asList().toList().forEach { previous.appendChild(it) }
            remove()
        } else if (parentElement?.childNodes?.length == 1) {
            // this was the only item left: unwrap it into the parent list's parent, and drop the list
            childNodes.asList().toList().forEach { parentElement?.after(it) }
            parentElement?.remove()
        }
    }

    override fun mergeNextIn() {
        val next = nextElementSibling
        if (next is ListItem) {
            next.childNodes.asList().toList().forEach { appendChild(it) }
            next.remove()
        }
    }

    override fun elementEndWithEndOfLineEquivalent(): Boolean =
        (textContent?.isNotEmpty() == true) || children.length > 0

    override fun containsMarkdownTextContent(): Boolean = true
}
