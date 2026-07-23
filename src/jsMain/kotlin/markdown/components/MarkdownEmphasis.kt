package markdown.components

import element
import markdown.InlineElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot

class MarkdownEmphasis : InlineElement() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).element("i") { slot() }
    }

    override fun getMarkdown(): String = "*" + super.getMarkdown().trim() + "*"

    override fun containsMarkdownTextContent(): Boolean = true
}
