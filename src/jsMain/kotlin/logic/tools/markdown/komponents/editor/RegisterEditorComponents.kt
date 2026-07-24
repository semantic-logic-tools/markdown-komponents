package logic.tools.markdown.komponents.editor

import logic.tools.markdown.komponents.define
import kotlinx.browser.window

fun registerEditorComponents() {
    window.customElements.define("toolbar-button", ToolbarButton::class.js, arrayOf("highlighted"))
    window.customElements.define("toolbar-separator", ToolbarSeparator::class.js)
    window.customElements.define("toolbar-dropdown", ToolbarDropdown::class.js)
    window.customElements.define("dropdown-element", DropdownElement::class.js)
    window.customElements.define("dropdown-elements", DropdownElements::class.js)
    window.customElements.define("toolbar-icon", ToolbarIcon::class.js, arrayOf("icon"))
    window.customElements.define("toggle-toolbar-button", ToggleToolbarButton::class.js, arrayOf("highlighted", "icon"))
    window.customElements.define("bold-toolbar-button", BoldToolbarButton::class.js, arrayOf("highlighted"))

    window.customElements.define("markdown-toolbar", MarkdownToolbar::class.js)

    window.customElements.define("markdown-document", MarkdownDocument::class.js)
    window.customElements.define("markdown-editor", MarkdownEditor::class.js)
}
