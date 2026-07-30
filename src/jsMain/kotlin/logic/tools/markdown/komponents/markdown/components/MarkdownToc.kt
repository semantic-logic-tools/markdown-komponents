@file:OptIn(ExperimentalJsExport::class)

package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.BaseWebComponent
import kotlinx.browser.document
import kotlinx.browser.window
import logic.tools.markdown.komponents.markdown.Heading
import logic.tools.markdown.komponents.markdown.MarkdownElement
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.asList
import logic.tools.markdown.komponents.style

/**
 * Not a [logic.tools.markdown.komponents.markdown.MarkdownComponent] — like the original, this is a plain component that only
 * implements [MarkdownElement] so it round-trips through `getMarkdown()` as a `${toc}` placeholder;
 * it isn't itself editable markdown content.
 */
@JsExport
class MarkdownToc : BaseWebComponent(), MarkdownElement {
    override val mustBeDirectChildOfDocument = false

    private var content: Element? = null

    var markdownDocument: Element? = null
        set(value) {
            field = value
            refresh()
        }

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).style(
            """
            :host {
              display: block;
            }
            @media screen {
              :host(.floating) {
                position: absolute;
                right: 0px;
                top: 0px;
              }
              :host(.floating) .level-2 {
                height: 0px;
                visibility: collapse;
              }
              :host(.floating:hover) .level-2 {
                height: auto;
                visibility: visible;
              }
            }
            .level {
              padding-left: 10px;
              font-size: 0.9em;
            }
            .level a {
              display: block;
            }
            div {
              display: flex;
              flex-direction: column;
            }
            """.trimIndent()
        )

        upgradeProperty("markdownDocument")
    }

    override fun connectedCallback() {
        super.connectedCallback()
        if (markdownDocument == null) {
            markdownDocument = closest("markdown-document")
            window.setTimeout({ refresh() }, 1000) // TODO: base this on a loaded/change event instead
        }
    }

    /** Rebuilds the table of contents from the current [markdownDocument]. Runs automatically when that property is set. */
    fun refresh() {
        val root = markdownDocument ?: return
        val shadow = shadowRoot ?: return

        content?.remove()
        var currentDepth = 1
        var currentList = document.createElement("div")
        content = currentList
        shadow.appendChild(currentList)

        val headings = root.querySelectorAll("*").asList().filterIsInstance<Heading>()
        headings.forEach { heading ->
            val a = document.createElement("a") as HTMLAnchorElement
            a.href = "#" + heading.id
            a.innerHTML = heading.innerHTML
            a.onclick = { heading.scrollIntoView() }

            while (currentDepth < heading.depth) {
                currentDepth++
                val nextList = document.createElement("div")
                nextList.classList.add("level")
                nextList.classList.add("level-$currentDepth")
                currentList.appendChild(nextList)
                currentList = nextList
            }
            while (currentDepth > heading.depth) {
                currentDepth--
                currentList = currentList.parentElement!!
            }
            currentList.appendChild(a)
        }
    }

    override fun getMarkdown(): String = "\${toc}"
}
