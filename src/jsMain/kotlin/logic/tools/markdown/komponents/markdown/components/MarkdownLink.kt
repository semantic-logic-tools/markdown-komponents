package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.element
import kotlinx.browser.window
import logic.tools.markdown.komponents.markdown.TerminalInlineElement
import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.events.KeyboardEvent
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

class MarkdownLink : TerminalInlineElement() {
    override val mustBeDirectChildOfDocument = false

    private val anchor: HTMLAnchorElement
    private val destinationInput: HTMLInputElement

    var destination: String
        get() = getAttribute("destination") ?: ""
        set(value) = setAttribute("destination", value)

    // `title` is inherited from HTMLElement itself (a native attribute-backed property already).

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        shadow.style(
            """
            :host {
              position: relative;
            }
            .show-link {
              user-select: none;
              position: absolute;
              background: white;
              padding: 3px;
              top: -8px;
              right: -20px;
              cursor: default;
              opacity: 0.0;
              transition: opacity 1s ease-in-out;
            }
            :host(:hover) .show-link, :host(.fresh) .show-link {
              opacity: 1.0;
            }
            .destination-input.visible {
              display: block;
            }
            .destination-input {
              display: none;
              position: absolute;
              z-index: 10;
              top: -15px;
              left: 15px;
              box-shadow: 0px 0px 5px 2px rgb(0 0 0 / 50%);
            }
            """.trimIndent()
        )
        anchor = (shadow.element("a") { setAttribute("part", "anchor") } as HTMLAnchorElement).also { it.slot() }
        val showLink = shadow.element("span") { className = "show-link"; textContent = "✎" }
        showLink.addEventListener("click", { showDestinationInput() })
        destinationInput = shadow.element("input") {
            setAttribute("placeholder", "http://")
            className = "destination-input"
        } as HTMLInputElement
        destinationInput.addEventListener("input", { onDestinationInput() })
        destinationInput.addEventListener("blur", { hideDestinationInput() })
        destinationInput.addEventListener("keydown", { event -> onDestinationKey(event as KeyboardEvent) })
    }

    override fun connectedCallback() {
        super.connectedCallback()
        window.setTimeout({ classList.remove("fresh") }, 5000)
        updateLinkTarget()
    }

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        if (name == "destination" || name == "title") {
            updateLinkTarget()
        }
    }

    /** Local links stay in the same window/tab; anything else opens in a new one. */
    private fun updateLinkTarget() {
        anchor.href = destination
        anchor.title = title
        anchor.target = if (destination.startsWith("#")) "_self" else "_blank"
        anchor.onclick = { event ->
            val continueDefault = dispatchEvent(CustomEvent("link-click", CustomEventInit(detail = destination, bubbles = true, cancelable = true)))
            if (!continueDefault) event.preventDefault()
        }
    }

    private fun onDestinationInput() {
        destination = destinationInput.value
        updateLinkTarget()
    }

    private fun hideDestinationInput() {
        classList.remove("fresh")
        destinationInput.classList.remove("visible")
    }

    private fun showDestinationInput() {
        destinationInput.classList.add("visible")
        destinationInput.focus()
        destinationInput.setSelectionRange(destinationInput.value.length, destinationInput.value.length)
    }

    private fun onDestinationKey(event: KeyboardEvent) {
        if (event.key == "Enter") {
            (event.target as? HTMLElement)?.blur()
        }
    }

    override fun newEmptyElementNameAfterBreak(): String? = null // after a link, don't auto-continue into another one

    override fun getMarkdown(): String =
        if (title.isNotEmpty()) "[$innerText]($destination \"$title\")" else "[$innerText]($destination)"

    override fun containsMarkdownTextContent(): Boolean = true
}
