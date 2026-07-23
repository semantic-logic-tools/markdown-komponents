package markdown.components

import element
import markdown.ListItem
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot
import style

open class MarkdownListItem : ListItem() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  position: relative;
                  left: 20px;
                }
                """.trimIndent()
            )
            element("div") { className = "item-container"; slot() }
        }
    }
}
