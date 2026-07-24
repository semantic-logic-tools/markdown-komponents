package logic.tools.markdown.komponents.editor

/**
 * What [MarkdownDocument] needs from a toolbar. The real `markdown-toolbar` (`Toolbar` in the
 * original TS) hasn't been ported yet — when it is, it should implement this. Until then,
 * `document.toolbar` can hold any `Element` (for `classList` toggling); a toolbar that also
 * implements this interface additionally gets the highlight/dropdown-title callbacks.
 */
interface EditorToolbar {
    fun highlightBoldButton()
    fun removeBoldButtonHighlighting()
    fun highlightItalicButton()
    fun removeItalicButtonHighlighting()
    fun highlightStrikeButton()
    fun removeStrikeButtonHighlighting()
    fun setDropdownTitle(title: String)
}
