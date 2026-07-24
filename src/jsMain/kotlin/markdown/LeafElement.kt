package markdown

import Selection
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.Text
import org.w3c.dom.asList

abstract class LeafElement : BlockElement() {

    /** True for a leaf that has no content by design (e.g. a thematic break) — being childless doesn't mean it's stale. */
    open fun isIntentionallyEmpty(): Boolean = false

    /*
      normalize for a leaf element consists of finding <br>s and:
      - create a new element of the same type with the content after the <br>
      - remove the <br>
      - move the content after the <br> (now in the newly created element) up to the parent

      <parent>content1 <leaf>content2 <br/> content3</leaf> content4</parent>
          becomes
      <parent>content1 <leaf>content2 </leaf><leaf> content3</leaf> content4</parent>
    */
    override fun normalizeContent(): Boolean {
        normalize()
        if (childNodes.length == 0 && !isIntentionallyEmpty()) {
            remove() // an empty leaf serves no purpose
            return true
        }

        // remove a double <br>: some browsers occasionally insert two on the same line.
        var sawBr = false
        for (content in childNodes.asList()) {
            if (content is HTMLBRElement) {
                if (sawBr) {
                    removeChild(content)
                    break
                }
                sawBr = true
            }
        }

        childNodes.asList().forEach { content ->
            if (content is HTMLBRElement) {
                pushNodesAfterBreakToParent(content)
                removeChild(content)
                if (childNodes.length == 0) {
                    // nothing left after removing the <br>: keep a ZWSP or the whole element may get removed.
                    appendChild(document.createTextNode(ZERO_WIDTH_SPACE))
                }
                return true
            } else if (content is MarkdownComponent) {
                if (content.normalizeContent()) return normalizeContent()
            } else if (content is Text) {
                val hasZwsp = (content.textContent?.indexOf(ZERO_WIDTH_SPACE) ?: -1) >= 0
                if (content.length > 1 && hasZwsp) {
                    // the ZWSP was there to keep the element non-empty; it has no purpose once there's real content
                    content.textContent = content.textContent?.replace(ZERO_WIDTH_SPACE, "")
                }
            } else if (normalizeUnknownChild(content)) {
                return true
            }
        }
        return false
    }

    override fun mergeWithPrevious(currentSelection: Selection?) {
        mergeWith(currentSelection, previousElementSibling)
    }

    fun mergeWith(currentSelection: Selection?, other: Element?) {
        if (other == null) return
        if (other is LeafElement) {
            if (currentSelection?.containsNode(this, true) == true) {
                other.setSelectionToEnd(currentSelection)
            }
            childNodes.asList().toList().forEach { other.appendChild(it) }
            remove()
        } else if (other is ContainerElement) {
            mergeWith(currentSelection, other.lastElementChild)
        }
    }

    override fun mergeNextIn() {
        val next = nextElementSibling
        if (next is LeafElement) {
            next.childNodes.asList().toList().forEach { appendChild(it) }
            next.remove()
        }
    }

    override fun elementEndWithEndOfLineEquivalent(): Boolean =
        (textContent?.isNotEmpty() == true) || children.length > 0
}
