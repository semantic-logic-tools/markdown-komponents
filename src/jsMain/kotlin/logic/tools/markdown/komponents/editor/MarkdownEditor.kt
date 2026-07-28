@file:OptIn(ExperimentalJsExport::class)

package logic.tools.markdown.komponents.editor

import logic.tools.markdown.komponents.element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot

@JsExport
class MarkdownEditor : BaseMarkdownDocument() {
    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        val toolbarElement = shadow.element("markdown-toolbar")
        shadow.slot()
        setToolbar(toolbarElement)
    }
}
