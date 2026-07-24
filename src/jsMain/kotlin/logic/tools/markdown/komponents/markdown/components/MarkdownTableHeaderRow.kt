package logic.tools.markdown.komponents.markdown.components

class MarkdownTableHeaderRow : MarkdownTableRow() {
    // TODO: aligns
    override fun getMarkdown(): String = super.getMarkdown() + "\n" + "| " + "--- |".repeat(children.length)
}
