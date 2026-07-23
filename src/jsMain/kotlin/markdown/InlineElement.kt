package markdown

import Selection
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.asList

abstract class InlineElement : MarkdownComponent() {
    override val mustBeDirectChildOfDocument = false

    /*
      normalize for an inline element consists of finding <br>s and:
      - create a new element of the same type with the content after the <br>
      - move the <br> to the parent, next to this
      - move the content after (now in the newly created element) to the parent, after the <br>

      <parent>content1 <inline>content2 <br/> content3</inline> content4</parent>
          becomes
      <parent>content1 <inline>content2 </inline><br/><inline> content3</inline> content4</parent>

      It is up to the parent to deal with the <br> at this point.
    */
    override fun normalizeContent(): Boolean {
        val next = nextSibling
        if (mergeSameSiblings() && next is InlineElement && tagName == next.tagName) {
            next.childNodes.asList().toList().forEach { appendChild(it) }
            parentNode?.removeChild(next)
            normalizeContent()
            return true
        }
        childNodes.asList().forEach { content ->
            if (content is HTMLBRElement) {
                pushBreakAndNodesAfterToParent(content)
                return true
            } else if (content is MarkdownComponent) {
                if (content.normalizeContent()) return normalizeContent()
            } else if (normalizeUnknownChild(content)) {
                return true
            }
        }
        return false
    }

    open fun mergeSameSiblings(): Boolean = false

    override fun getMarkdown(): String = getMarkdownWithTextForElement()

    override fun mergeWithPrevious(currentSelection: Selection?) {
        (parentNode as? MarkdownComponent)?.mergeWithPrevious(currentSelection)
    }

    override fun mergeNextIn() {
        (parentNode as? MarkdownComponent)?.mergeNextIn()
    }
}

abstract class TerminalInlineElement : MarkdownComponent()
