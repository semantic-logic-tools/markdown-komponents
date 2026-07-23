package markdown.components

import markdown.InlineElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot
import style

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
