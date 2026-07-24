package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.markdown.InlineElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

class MarkdownStrong : InlineElement() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(":host { font-weight: bold; }")
            slot()
        }
    }

    override fun getMarkdown(): String = "**" + super.getMarkdown().trim() + "**"

    override fun mergeSameSiblings(): Boolean = true

    override fun containsMarkdownTextContent(): Boolean = true
}
