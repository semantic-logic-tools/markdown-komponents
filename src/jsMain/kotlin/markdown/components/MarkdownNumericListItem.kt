package markdown.components

import element
import markdown.ListItem
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot
import style

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
