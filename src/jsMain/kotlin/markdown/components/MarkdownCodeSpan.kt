package markdown.components

import element
import markdown.TerminalInlineElement
import markdown.getMarkdownWithTextForElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot

class MarkdownCodeSpan : TerminalInlineElement() {
    override val mustBeDirectChildOfDocument = false

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).element("code") { slot() }
    }

    override fun getMarkdown(): String = "`" + getMarkdownWithTextForElement() + "`"

    override fun containsMarkdownTextContent(): Boolean = true
}
