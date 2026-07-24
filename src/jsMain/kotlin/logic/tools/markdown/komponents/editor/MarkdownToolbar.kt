package logic.tools.markdown.komponents.editor

import logic.tools.markdown.komponents.BaseWebComponent
import logic.tools.markdown.komponents.element
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

/**
 * A from-scratch redo of the original `Toolbar`, not a straight port: dead code dropped (align,
 * indent, clipboard and checkbox buttons were commented out in the original and never wired up),
 * and button references are kept directly from construction instead of re-discovered later via
 * `querySelector('.bold')`-style class-name lookups.
 */
class MarkdownToolbar : BaseWebComponent(), EditorToolbar {

    var markdownDocument: BaseMarkdownDocument? = null

    private val boldButton: ToggleToolbarButton
    private val italicButton: ToggleToolbarButton
    private val strikeButton: ToggleToolbarButton
    private val dropdownTitleSpan: Element

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        shadow.style(
            """
            :host(.focus-disabled:hover) {
              opacity: 1.0;
            }
            :host {
              display: block;
              border: solid 1px gray;
              padding: 5px;
            }
            :host(.focus-disabled) {
              opacity: 0.1;
            }
            .toolbar {
              height: 24px;
              display: flex;
            }
            .looks-like-header1 { font-size: var(--header1-font-size); font-weight: bold; }
            .looks-like-header2 { font-size: var(--header2-font-size); font-weight: bold; }
            .looks-like-header3 { font-size: var(--header3-font-size); font-weight: bold; }
            .looks-like-header4 { font-size: var(--header4-font-size); font-weight: bold; }
            .looks-like-header5 { font-size: var(--header5-font-size); font-weight: bold; }
            .looks-like-header6 { font-size: var(--header6-font-size); font-weight: bold; }
            @media print {
              :host {
                display: none;
              }
            }
            """.trimIndent()
        )

        val toolbarRow = shadow.element("div") { className = "toolbar" }

        val dropdown = toolbarRow.element("toolbar-dropdown")
        dropdownTitleSpan = dropdown.element("span") { className = "dropdown-title"; textContent = "Paragraph" }
        dropdown.appendChild(document.createTextNode(" ▾"))
        val dropdownElements = dropdown.element("dropdown-elements") { setAttribute("slot", "dropdown-elements") }
        headingOption(dropdownElements, 1) { markdownDocument?.header1Element() }
        headingOption(dropdownElements, 2) { markdownDocument?.header2Element() }
        headingOption(dropdownElements, 3) { markdownDocument?.header3Element() }
        headingOption(dropdownElements, 4) { markdownDocument?.header4Element() }
        headingOption(dropdownElements, 5) { markdownDocument?.header5Element() }
        headingOption(dropdownElements, 6) { markdownDocument?.header6Element() }
        paragraphOption(dropdownElements) { markdownDocument?.pararaphElement() }

        toolbarRow.element("toolbar-separator")

        boldButton = toggleButton(toolbarRow, "bold", "toggle bold") { onBoldClick() }
        italicButton = toggleButton(toolbarRow, "italic", "toggle italic") { onItalicClick() }
        strikeButton = toggleButton(toolbarRow, "strikethrough", "toggle strike") { onStrikeClick() }

        toolbarRow.element("toolbar-separator")

        actionButton(toolbarRow, "list-bulleted", "list") { markdownDocument?.listBulletedClick() }
        actionButton(toolbarRow, "list-numbered", "numbered list") { markdownDocument?.listNumericClick() }

        toolbarRow.element("toolbar-separator")

        actionButton(toolbarRow, "code-inline", "code inline") { markdownDocument?.makeCodeInline() }
        actionButton(toolbarRow, "break", "break") { markdownDocument?.makeBreak() }
        actionButton(toolbarRow, "insert-photo", "insert image") { markdownDocument?.insertPhoto(null, null) }
        actionButton(toolbarRow, "insert-link", "insert link") { markdownDocument?.insertLink() }
        actionButton(toolbarRow, "code-block", "insert code block") { markdownDocument?.makeCodeBlock() }
        actionButton(toolbarRow, "quote", "insert quote") { markdownDocument?.makeQuoteBlock() }

        toolbarRow.element("toolbar-separator")
        toolbarRow.element("slot") { setAttribute("name", "toolbar") }

        shadow.slot()
    }

    override fun connectedCallback() {
        super.connectedCallback()
        // clicking the toolbar must not steal the document's text selection
        addEventListener("mousedown", { event -> event.preventDefault() })
    }

    fun setMarkdownDocument(document: BaseMarkdownDocument) {
        markdownDocument = document
    }

    private fun actionButton(parent: Element, icon: String, titleText: String, onClick: () -> Unit): Element {
        val button = parent.element("toolbar-button") { setAttribute("title", titleText) }
        button.element("toolbar-icon") { setAttribute("icon", icon) }
        button.addEventListener("click", { onClick() })
        return button
    }

    private fun toggleButton(parent: Element, icon: String, titleText: String, onClick: () -> Unit): ToggleToolbarButton {
        val button = parent.element("toggle-toolbar-button") {
            setAttribute("title", titleText)
            setAttribute("icon", icon)
        } as ToggleToolbarButton
        button.addEventListener("click", { onClick() })
        return button
    }

    private fun headingOption(parent: Element, level: Int, onClick: () -> Unit) {
        val item = parent.element("dropdown-element")
        item.element("div") { className = "looks-like-header$level"; textContent = "Heading $level" }
        item.addEventListener("mousedown", { onClick() })
    }

    private fun paragraphOption(parent: Element, onClick: () -> Unit) {
        val item = parent.element("dropdown-element")
        item.element("markdown-paragraph") { textContent = "Paragraph" }
        item.addEventListener("mousedown", { onClick() })
    }

    private fun onBoldClick() {
        if (boldButton.highlighted) markdownDocument?.removeBold() else markdownDocument?.makeBold()
    }

    private fun onItalicClick() {
        if (italicButton.highlighted) markdownDocument?.removeItalic() else markdownDocument?.makeItalic()
    }

    private fun onStrikeClick() {
        if (strikeButton.highlighted) markdownDocument?.removeStrike() else markdownDocument?.makeStrike()
    }

    override fun highlightBoldButton() { boldButton.highlighted = true }
    override fun removeBoldButtonHighlighting() { boldButton.highlighted = false }
    override fun highlightItalicButton() { italicButton.highlighted = true }
    override fun removeItalicButtonHighlighting() { italicButton.highlighted = false }
    override fun highlightStrikeButton() { strikeButton.highlighted = true }
    override fun removeStrikeButtonHighlighting() { strikeButton.highlighted = false }
    override fun setDropdownTitle(title: String) {
        dropdownTitleSpan.textContent = title
    }
}
