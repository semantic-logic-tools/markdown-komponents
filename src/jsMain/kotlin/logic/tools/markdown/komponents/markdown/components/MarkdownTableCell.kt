package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.markdown.TableCell

class MarkdownTableCell : TableCell() {
    init {
        renderCell("display: table-cell; border: lightgrey 1px solid;")
    }
}
