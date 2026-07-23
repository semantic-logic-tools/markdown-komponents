package editor

import BaseWebComponent
import element
import org.w3c.dom.Element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot
import style

class ToolbarButton : BaseWebComponent() {

    private val button: Element

    var highlighted: Boolean
        get() = hasAttribute("highlighted")
        set(value) = if (value) setAttribute("highlighted", "") else removeAttribute("highlighted")

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        shadow.style(
            """
            button {
              padding: 0;
              margin: 0;
              border: none;
              background-color: transparent;
              vertical-align: center;
              height: 24px;
            }
            button:hover {
              background-color: lightblue;
            }
            .highlighted {
              background-color: gray;
            }
            """.trimIndent()
        )
        button = shadow.element("button") { slot() }
    }

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        if (name == "highlighted") {
            if (newValue != null) button.classList.add("highlighted") else button.classList.remove("highlighted")
        }
    }
}
