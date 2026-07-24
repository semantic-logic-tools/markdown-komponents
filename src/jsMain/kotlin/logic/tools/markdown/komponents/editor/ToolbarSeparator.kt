package logic.tools.markdown.komponents.editor

import logic.tools.markdown.komponents.BaseWebComponent
import logic.tools.markdown.komponents.element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.style

class ToolbarSeparator : BaseWebComponent() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(".separator { width: 1px; background-color: gray; height: 24px; margin: 0 4px 0 4px; }")
            element("div") { className = "separator" }
        }
    }
}
