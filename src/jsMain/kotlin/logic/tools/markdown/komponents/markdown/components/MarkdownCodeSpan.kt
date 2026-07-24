package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.element
import logic.tools.markdown.komponents.markdown.TerminalInlineElement
import logic.tools.markdown.komponents.markdown.getMarkdownWithTextForElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot

class MarkdownCodeSpan : TerminalInlineElement() {
    override val mustBeDirectChildOfDocument = false

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).element("code") { slot() }
    }

    override fun getMarkdown(): String = "`" + getMarkdownWithTextForElement() + "`"

    override fun containsMarkdownTextContent(): Boolean = true
}
