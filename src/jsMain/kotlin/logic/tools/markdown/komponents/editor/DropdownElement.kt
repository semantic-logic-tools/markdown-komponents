package logic.tools.markdown.komponents.editor

import logic.tools.markdown.komponents.BaseWebComponent
import logic.tools.markdown.komponents.element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

class DropdownElement : BaseWebComponent() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(".clickable:hover { background-color: lightblue; cursor: pointer; }")
            element("div") { className = "clickable"; slot() }
        }
    }
}
