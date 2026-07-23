package editor

import element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot

class MarkdownEditor : BaseMarkdownDocument() {
    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        val toolbarElement = shadow.element("markdown-toolbar")
        shadow.slot()
        setToolbar(toolbarElement)
    }
}
