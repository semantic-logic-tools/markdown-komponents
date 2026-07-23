package markdown.components

import markdown.TableCell

class MarkdownTableCell : TableCell() {
    init {
        renderCell("display: table-cell; border: lightgrey 1px solid;")
    }
}
