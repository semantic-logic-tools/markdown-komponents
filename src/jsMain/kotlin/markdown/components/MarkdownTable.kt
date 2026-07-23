package markdown.components

import markdown.ContainerElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.asList
import slot
import style

class MarkdownTable : ContainerElement() {
    override val mustBeDirectChildOfDocument = true

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  display: table;
                  border-collapse: collapse;
                }
                """.trimIndent()
            )
            slot()
        }
    }

    override fun getMarkdown(): String =
        "\n" + children.asList().joinToString("\n") { child -> if (child is MarkdownTableRow) child.getMarkdown() else "" } + "\n"

    override fun containsMarkdownTextContent(): Boolean = false
}
