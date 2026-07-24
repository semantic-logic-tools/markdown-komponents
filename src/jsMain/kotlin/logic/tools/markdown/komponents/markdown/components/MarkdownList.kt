package logic.tools.markdown.komponents.markdown.components

import kotlinx.browser.document
import logic.tools.markdown.komponents.markdown.ContainerElement
import logic.tools.markdown.komponents.markdown.MarkdownElementWithLevel
import org.w3c.dom.Element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.asList
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

class MarkdownList : ContainerElement(), MarkdownElementWithLevel {
    override val mustBeDirectChildOfDocument = false

    var ordered: Boolean
        get() = getAttribute("ordered") == "true"
        set(value) = setAttribute("ordered", value.toString())

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  counter-reset: section;
                }
                :host ::slotted(*) {
                  display: list-item;
                }
                :host([ordered='true']) ::slotted(*) {
                  list-style-type: decimal;
                }
                """.trimIndent()
            )
            slot()
        }
    }

    override fun getMarkdown(): String = super.getMarkdown() + "\n"

    override fun containsMarkdownTextContent(): Boolean = false

    override fun goDownOneLevel(child: Element?) {
        if (child == null) return
        val list = document.createElement("markdown-list")
        val item = document.createElement("markdown-list-item")
        item.innerHTML = child.innerHTML
        list.appendChild(item)
        child.innerHTML = "&nbsp"
        child.appendChild(list)
    }

    override fun goUpOneLevel(child: Element?) {
        if (child == null) return
        val nextUp = parentElement?.closest("markdown-list-item") ?: return
        // snapshot first: `after` moves nodes out of this element's live childNodes as it goes
        val nodes = childNodes.asList()
        val indexOfChild = nodes.indexOf(child)
        nodes.drop(indexOfChild).forEach { nextUp.after(it) }
    }
}
