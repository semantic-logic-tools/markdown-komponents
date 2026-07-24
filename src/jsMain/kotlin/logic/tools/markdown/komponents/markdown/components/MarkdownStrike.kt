package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.element
import logic.tools.markdown.komponents.markdown.InlineElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot

class MarkdownStrike : InlineElement() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).element("del") { slot() }
    }

    override fun getMarkdown(): String = "~~" + super.getMarkdown().trim() + "~~"

    override fun containsMarkdownTextContent(): Boolean = true
}
