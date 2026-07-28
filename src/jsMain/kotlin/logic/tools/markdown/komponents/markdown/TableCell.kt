package logic.tools.markdown.komponents.markdown

import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.OPEN
import org.w3c.dom.asList
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

/**
 * Shared behavior for `markdown-table-cell` and `markdown-table-header-cell`. Kept separate from
 * [MarkdownComponent] subclassing directly so each concrete cell type can build its own shadow DOM
 * (with different `:host` CSS) from its own `init` — a component can only call `attachShadow` once,
 * so this can't build one itself and also be a base class for a differently-styled concrete cell.
 */
abstract class TableCell : ContainerElement() {
    override val mustBeDirectChildOfDocument = false

    protected fun renderCell(hostCss: String) {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(":host { $hostCss }")
            slot()
        }
    }

    override fun getMarkdown(): String =
        childNodes.asList().joinToString("") { child ->
            val markdownElement = asMarkdownElement(child)
            when {
                markdownElement != null -> markdownElement.getMarkdown()
                child.textContent?.replace(Regex("\\s"), "")?.isEmpty() == true -> ""
                else -> child.textContent?.replace(Regex("^\\s+", RegexOption.MULTILINE), " ") ?: ""
            }
        }

    override fun containsMarkdownTextContent(): Boolean = true
}
