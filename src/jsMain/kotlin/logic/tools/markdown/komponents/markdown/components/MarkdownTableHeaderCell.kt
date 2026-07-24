package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.markdown.TableCell

class MarkdownTableHeaderCell : TableCell() {

    /** "left" | "right" | "center", not yet used to affect rendering — carried through from the original. */
    var align: String?
        get() = getAttribute("align")
        set(value) = if (value == null) removeAttribute("align") else setAttribute("align", value)

    init {
        renderCell("display: table-cell; border: lightgrey 1px solid; background-color: lightgray;")
    }
}
