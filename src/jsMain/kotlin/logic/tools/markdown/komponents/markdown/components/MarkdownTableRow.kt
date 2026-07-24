package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.markdown.ContainerElement
import logic.tools.markdown.komponents.markdown.MarkdownElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.asList
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

open class MarkdownTableRow : ContainerElement() {
    override val mustBeDirectChildOfDocument = false

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  display: table-row;
                  border: lightgrey 1px solid;
                }
                """.trimIndent()
            )
            slot()
        }
    }

    // TODO: nicer output — align column widths, etc.
    override fun getMarkdown(): String =
        "| " + children.asList().joinToString(" | ") { child -> if (child is MarkdownElement) child.getMarkdown() else "" } + " |"

    override fun containsMarkdownTextContent(): Boolean = false
}
