package editor

import BaseWebComponent
import element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot
import style

class DropdownElement : BaseWebComponent() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(".clickable:hover { background-color: lightblue; cursor: pointer; }")
            element("div") { className = "clickable"; slot() }
        }
    }
}
