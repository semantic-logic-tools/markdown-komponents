package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.element
import logic.tools.markdown.komponents.markdown.ListItem
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

class MarkdownNumericListItem : ListItem() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  position: relative;
                  left: 20px;
                  display: list-item;
                  counter-increment: numbered-item;
                }
                :host::marker {
                  content: counters(numbered-item, ".") ". ";
                }
                """.trimIndent()
            )
            element("div") { className = "item-container"; slot() }
        }
    }
}
