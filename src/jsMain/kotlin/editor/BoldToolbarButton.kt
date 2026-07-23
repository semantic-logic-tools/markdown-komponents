package editor

import BaseWebComponent
import element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode

class BoldToolbarButton : BaseWebComponent() {

    private val toolbarButton: ToolbarButton

    var highlighted: Boolean
        get() = hasAttribute("highlighted")
        set(value) = if (value) setAttribute("highlighted", "") else removeAttribute("highlighted")

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        toolbarButton = shadow.element("toolbar-button") {
            element("toolbar-icon") { setAttribute("icon", "bold") }
        } as ToolbarButton
    }

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        if (name == "highlighted") {
            toolbarButton.highlighted = highlighted
        }
    }
}
