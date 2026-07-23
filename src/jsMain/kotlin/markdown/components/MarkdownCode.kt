package markdown.components

import element
import hljs
import markdown.LeafElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot

class MarkdownCode : LeafElement() {
    override val mustBeDirectChildOfDocument = true

    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).element("pre").element("code") { slot() }
    }

    override fun getMarkdown(): String {
        val lang = getAttribute("lang")
        val id = getAttribute("id")
        return if (id != null) "``` $lang {$id}\n$textContent\n```\n" else "``` $lang\n$textContent\n```\n"
    }

    override fun connectedCallback() {
        super.connectedCallback()
        highlight()
        addEventListener("input", { highlight() })
    }

    private fun highlight() {
        val lang = getAttribute("lang")
        val text = textContent
        if (lang != null && text != null) {
            innerHTML = hljs.highlight(lang, text).value
        }
    }

    override fun containsMarkdownTextContent(): Boolean = true
}
