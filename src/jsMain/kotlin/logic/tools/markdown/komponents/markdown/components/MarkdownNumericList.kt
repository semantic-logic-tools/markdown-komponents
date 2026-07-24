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

class MarkdownNumericList : ContainerElement(), MarkdownElementWithLevel {
    override val mustBeDirectChildOfDocument = false

    var ordered: Boolean
        get() = getAttribute("ordered") == "true"
        set(value) = setAttribute("ordered", value.toString())

    var start: Int?
        get() = getAttribute("start")?.toIntOrNull()
        set(value) = if (value == null) removeAttribute("start") else setAttribute("start", value.toString())

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  /* fallback for browsers without ::marker { content } support */
                  list-style-type: decimal;
                  /* a real <ol> gets a fresh counter scope per nesting level for free (it's baked
                     into the UA stylesheet); a custom element doesn't, so this does it explicitly —
                     without it, nested lists just continue the outer count instead of showing 1.1 */
                  counter-reset: numbered-item;
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
        val list = document.createElement("markdown-numeric-list")
        val item = document.createElement("markdown-numeric-list-item")
        item.innerHTML = child.innerHTML
        list.appendChild(item)
        child.innerHTML = "&nbsp"
        child.appendChild(list)
    }

    override fun goUpOneLevel(child: Element?) {
        if (child == null) return
        val nextUp = parentElement?.closest("markdown-numeric-list-item") ?: return
        val nodes = childNodes.asList()
        val indexOfChild = nodes.indexOf(child)
        nodes.drop(indexOfChild).forEach { nextUp.after(it) }
    }
}
