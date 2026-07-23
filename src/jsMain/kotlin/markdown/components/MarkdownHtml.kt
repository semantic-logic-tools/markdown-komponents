package markdown.components

import markdown.LeafElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot

class MarkdownHtml : LeafElement() {
    override val mustBeDirectChildOfDocument = false

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).slot()
    }

    override fun isEditable(): Boolean = false

    override fun getMarkdown(): String = innerHTML.trim() + "\n\n"

    override fun containsMarkdownTextContent(): Boolean = true
}
