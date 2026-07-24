package logic.tools.markdown.komponents.markdown

import kotlinx.browser.document
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Node
import org.w3c.dom.asList

/**
 * Generic recursive normalize, used as the default by [MarkdownComponent]s (e.g. [ContainerElement])
 * that don't need their own normalize logic.
 */
fun normalizeContent(element: Node): Boolean {
    element.childNodes.asList().forEach { content ->
        if (content is MarkdownComponent) {
            if (content.normalizeContent()) return normalizeContent(element)
        } else if (normalizeUnknownChild(content)) {
            return true
        }
    }
    return false
}

/**
 * Fixes up children the parser or the browser produced that aren't markdown components yet: a
 * stray `<img>` (should be `<markdown-image>`), or a `<markdown-paragraph>` that browsers leave
 * containing only a lone `<br>` on an empty line (needs the zero-width-space placeholder instead).
 */
fun normalizeUnknownChild(content: Node): Boolean {
    if (content is HTMLImageElement) {
        val image = document.createElement("markdown-image")
        content.replaceWith(image)
        content.getAttribute("src")?.let { image.setAttribute("destination", it) }
        content.getAttribute("title")?.let { image.setAttribute("title", it) }
        content.getAttribute("alt")?.let { image.textContent = it }
        return true
    }
    if (content.nodeName.lowercase() == "markdown-paragraph") {
        val onlyChild = content.childNodes.asList().singleOrNull()
        if (onlyChild is HTMLBRElement) {
            onlyChild.remove()
            content.appendChild(document.createTextNode(ZERO_WIDTH_SPACE))
            return true
        }
    }
    return false
}
