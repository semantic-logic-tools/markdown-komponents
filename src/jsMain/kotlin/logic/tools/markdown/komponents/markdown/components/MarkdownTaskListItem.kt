package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.element
import logic.tools.markdown.komponents.markdown.ListItem
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

/**
 * Extends [ListItem] directly (not [MarkdownListItem]) — it needs the shared list-item logic but
 * builds a completely different shadow DOM (with a checkbox), and a component can only call
 * `attachShadow` once.
 */
class MarkdownTaskListItem : ListItem() {

    private val checkbox: HTMLInputElement

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        shadow.style(
            """
            :host {
              position: relative;
              left: 20px;
            }
            .item-container {
              padding-left: 20px;
            }
            input {
              position: absolute;
              z-index: 3;
            }
            .task-and-container {
              display: inline;
            }
            """.trimIndent()
        )
        val container = shadow.element("div") { className = "task-and-container" }
        checkbox = container.element("input") { setAttribute("type", "checkbox") } as HTMLInputElement
        checkbox.addEventListener("change", { setAttribute("checked", checkbox.checked.toString()) })
        container.element("div") { className = "item-container"; slot() }
    }

    var checked: Boolean
        get() = getAttribute("checked") == "true"
        set(value) = setAttribute("checked", value.toString())

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        if (name == "checked") {
            checkbox.checked = checked
        }
    }

    override fun getTaskMarkdown(): String = "[" + (if (checked) "x" else " ") + "] "
}
