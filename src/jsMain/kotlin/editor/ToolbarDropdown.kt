package editor

import BaseWebComponent
import element
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.events.Event
import slot
import style

class ToolbarDropdown : BaseWebComponent() {

    private val dropdownElementsSlot: Element
    private val hideOnOutsideMouseDown: (Event) -> Unit = { hideDropdown() }

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        shadow.style(
            """
            :host {
              position: relative;
              z-index: 10;
            }
            slot[name=dropdown-elements] {
              display: none;
              position: fixed;
            }
            """.trimIndent()
        )
        val button = shadow.element("toolbar-button") { slot() }
        button.addEventListener("mousedown", { event -> showDropdown(event) })
        dropdownElementsSlot = shadow.element("slot") { setAttribute("name", "dropdown-elements") }
    }

    override fun connectedCallback() {
        super.connectedCallback()
        document.addEventListener("mousedown", hideOnOutsideMouseDown)
    }

    override fun disconnectedCallback() {
        document.removeEventListener("mousedown", hideOnOutsideMouseDown)
    }

    private fun showDropdown(event: Event) {
        event.stopPropagation()
        (dropdownElementsSlot as HTMLElement).style.display = "block"
    }

    private fun hideDropdown() {
        (dropdownElementsSlot as HTMLElement).style.display = "none"
    }
}
