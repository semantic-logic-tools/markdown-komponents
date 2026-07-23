import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.Node

/** Small typed builders for constructing DOM trees imperatively. */

/** Creates a detached element; the caller decides where it gets attached. */
fun createElement(tagName: String, content: Element.() -> Unit = {}): Element {
    val child = document.createElement(tagName)
    child.content()
    return child
}

/** Creates a child element and immediately appends it — handy when building shadow DOM in `init`. */
fun Node.element(tagName: String, content: Element.() -> Unit = {}): Element =
    createElement(tagName, content).also { appendChild(it) }

fun Node.slot(name: String? = null): Element = element("slot") {
    name?.let { setAttribute("name", it) }
}

fun Node.style(css: String): Element = element("style") {
    textContent = css
}
