package markdown

import element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot
import style

abstract class Heading : LeafElement() {
    abstract val depth: Int
    override val mustBeDirectChildOfDocument = true

    override fun getMarkdown(): String = "#".repeat(depth) + " " + getMarkdownWithTextForElement() + "\n"

    override fun newEmptyElementNameAfterBreak(): String = "markdown-paragraph" // after a title, we typically want a paragraph

    override fun containsMarkdownTextContent(): Boolean = true

    /** Concrete subclasses call this from their `init` block once [depth] has been set. */
    protected fun renderHeading() {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  position: relative;
                  min-height: 1em;
                }
                h$depth {
                  font-size: var(--header$depth-font-size);
                }
                """.trimIndent()
            )
            element("h$depth") { slot() }
        }
    }
}
