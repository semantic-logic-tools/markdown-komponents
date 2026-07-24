package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.Selection
import logic.tools.markdown.komponents.element
import kotlinx.browser.document
import logic.tools.markdown.komponents.markdown.ContainerElement
import logic.tools.markdown.komponents.markdown.MarkdownElement
import logic.tools.markdown.komponents.markdown.MarkdownElementEscapeByBackspace
import logic.tools.markdown.komponents.markdown.MarkdownElementWithLevel
import org.w3c.dom.Element
import org.w3c.dom.OPEN
import org.w3c.dom.asList
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

class MarkdownQuote : ContainerElement(), MarkdownElementWithLevel, MarkdownElementEscapeByBackspace {
    override val mustBeDirectChildOfDocument = true

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  position: relative;
                }
                blockquote::before {
                  position: absolute;
                  width: 3px;
                  height: 100%;
                  left: 20px;
                  background-color: lightgray;
                  content: '';
                }
                """.trimIndent()
            )
            element("blockquote") { slot() }
        }
    }

    override fun getMarkdown(): String =
        // FIXME this should be per line, not per child
        childNodes.asList().joinToString("") { child ->
            "> " + if (child is MarkdownElement) child.getMarkdown() else (child.textContent ?: "")
        }

    override fun containsMarkdownTextContent(): Boolean = true

    override fun mergeWithPrevious(currentSelection: Selection?) {
        firstChild?.let { before(it) }
    }

    override fun goDownOneLevel(child: Element?) {
        if (child != null) {
            val quote = document.createElement("markdown-quote")
            child.replaceWith(quote)
            quote.append(child)
        }
    }

    override fun goUpOneLevel(child: Element?) {
        if (child != null && childNodes.length > 1) {
            // snapshot first: appending these into `quote` mutates this element's live childNodes
            val nodes = childNodes.asList()
            val indexOfChild = nodes.indexOf(child)
            if (indexOfChild < nodes.size - 1) {
                val quote = document.createElement("markdown-quote")
                after(quote)
                nodes.drop(indexOfChild + 1).forEach { quote.append(it) }
            }
            if (indexOfChild > 0) {
                after(child)
            } else {
                before(child)
            }
            if (childNodes.length == 0) {
                remove()
            }
        } else {
            // not sure we want to get the whole thing out? TODO maybe only if there's no quote parent, via closest()
            childNodes.asList().toList().forEach { before(it) }
            remove()
        }
    }

    override fun escapeByBackspace(child: Element?) {
        goUpOneLevel(child)
    }
}
