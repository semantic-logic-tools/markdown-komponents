package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.element
import logic.tools.markdown.komponents.markdown.LeafElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode

class MarkdownBreak : LeafElement() {
    override val mustBeDirectChildOfDocument = true

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).element("hr")
    }

    override fun getMarkdown(): String = "-----------------------\n"

    override fun containsMarkdownTextContent(): Boolean = false

    override fun isIntentionallyEmpty(): Boolean = true
}
