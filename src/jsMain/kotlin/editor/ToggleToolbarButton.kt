package editor

import BaseWebComponent
import element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode

class ToggleToolbarButton : BaseWebComponent() {

    private val toolbarButton: ToolbarButton
    private val toolbarIcon: ToolbarIcon

    var highlighted: Boolean
        get() = hasAttribute("highlighted")
        set(value) = if (value) setAttribute("highlighted", "") else removeAttribute("highlighted")

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        toolbarButton = shadow.element("toolbar-button") {} as ToolbarButton
        toolbarIcon = toolbarButton.element("toolbar-icon") {} as ToolbarIcon
    }

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        when (name) {
            "highlighted" -> toolbarButton.highlighted = highlighted
            "icon" -> toolbarIcon.icon = newValue ?: ""
        }
    }
}
