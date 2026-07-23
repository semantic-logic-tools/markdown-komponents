package markdown

import org.w3c.dom.Node
import org.w3c.dom.asList

abstract class ContainerElement : MarkdownComponent() {

    override fun contentLength(): Int {
        var result = 0
        childNodes.asList().forEach { child ->
            if (child is MarkdownComponent) result += child.contentLength()
        }
        return result + endOfLineEquivalentLength()
    }

    override fun contentLengthUntil(child: Node?): Int {
        val nodes = childNodes.asList()
        val indexOfChild = if (child == null) -1 else nodes.indexOf(child)
        var result = 0
        if (indexOfChild >= 0) {
            nodes.subList(0, indexOfChild).forEach { sibling ->
                if (sibling is MarkdownComponent) result += sibling.contentLength()
            }
        }
        return result
    }

    override fun elementEndWithEndOfLineEquivalent(): Boolean = children.length > 0
}
