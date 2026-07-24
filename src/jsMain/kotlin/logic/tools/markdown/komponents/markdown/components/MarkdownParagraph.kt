package logic.tools.markdown.komponents.markdown.components

import kotlinx.browser.document
import logic.tools.markdown.komponents.markdown.LeafElement
import logic.tools.markdown.komponents.markdown.MarkdownComponent
import logic.tools.markdown.komponents.markdown.MarkdownElement
import logic.tools.markdown.komponents.markdown.ZERO_WIDTH_SPACE
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.OPEN
import org.w3c.dom.asList
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

/**
 * Markdown paragraphs can only contain #text nodes, inline markdown components, or a `<br>` —
 * [normalizeContent] keeps a zero-width space in it so it doesn't disappear while empty.
 */
class MarkdownParagraph : LeafElement() {
    override val mustBeDirectChildOfDocument = true

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  position: relative;
                  display: block;
                  margin-block-start: 1em;
                  margin-block-end: 1em;
                  margin-inline-start: 0px;
                  margin-inline-end: 0px;
                }
                """.trimIndent()
            )
            slot()
        }
    }

    override fun getMarkdown(): String =
        childNodes.asList().joinToString("") { child ->
            when {
                child is MarkdownElement -> child.getMarkdown()
                child.textContent?.replace(Regex("\\s"), "")?.isEmpty() == true -> ""
                else -> child.textContent?.replace(Regex("^\\s+", RegexOption.MULTILINE), " ") ?: ""
            }
        }.replace(ZERO_WIDTH_SPACE, "") + "\n\n"

    override fun containsMarkdownTextContent(): Boolean = true

    override fun normalizeContent(): Boolean {
        val onlyChild = childNodes.asList().singleOrNull()
        if (onlyChild is HTMLBRElement) {
            onlyChild.remove()
            appendChild(document.createTextNode(ZERO_WIDTH_SPACE))
        }
        if (lastChild is MarkdownComponent) {
            appendChild(document.createTextNode(ZERO_WIDTH_SPACE))
        }
        return super.normalizeContent()
    }

    fun isEmpty(): Boolean = getMarkdown() == "\n\n"
}
