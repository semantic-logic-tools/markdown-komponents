@file:OptIn(ExperimentalJsExport::class)

package logic.tools.markdown.komponents.editor

import logic.tools.markdown.komponents.BaseWebComponent
import logic.tools.markdown.komponents.Selection
import logic.tools.markdown.komponents.caretRangeFromPoint
import logic.tools.markdown.komponents.element
import logic.tools.markdown.komponents.getSelection
import kotlinx.browser.document
import kotlinx.browser.window
import logic.tools.markdown.komponents.markdown.MarkdownComponent
import logic.tools.markdown.komponents.markdown.MarkdownElementEscapeByBackspace
import logic.tools.markdown.komponents.markdown.MarkdownElementWithLevel
import logic.tools.markdown.komponents.markdown.ZERO_WIDTH_SPACE
import logic.tools.markdown.komponents.markdown.components.MarkdownImage
import logic.tools.markdown.komponents.markdown.components.MarkdownLink
import logic.tools.markdown.komponents.markdown.normalizeUnknownChild
import org.w3c.dom.ChildNode
import org.w3c.dom.DragEvent
import org.w3c.dom.Element
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Node
import org.w3c.dom.OPEN
import org.w3c.dom.Range
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.dom.Text
import org.w3c.dom.asList
import org.w3c.dom.clipboard.ClipboardEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.files.FileReader
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

private val defaultParser: (String) -> String = { md -> "<markdown-paragraph>$md</markdown-paragraph>" }

/**
 * `markdown-document`/`markdown-editor` share everything except their shadow DOM, which is built
 * once from each concrete class's own `init` (a component can only call `attachShadow` once).
 */
@JsExport
abstract class BaseMarkdownDocument : BaseWebComponent() {

    private var parserOverride: ((String) -> String)? = null

    /**
     * Converts markdown text to the HTML string of `<markdown-*>` tags this class sets as
     * `innerHTML`. Left for the library consumer to wire up — this placeholder only wraps the
     * text in a paragraph so the component still works standalone. To plug in a real parser,
     * implement `render.MarkdownRenderer` (or use the default `render.MarkdownComponentsRenderer`)
     * with whatever markdown engine you like; the optional `tsstack` package is a ready-made
     * `@ts-stack/markdown`-backed example (`doc.parser = tsstack::parseMarkdown`).
     *
     * An explicit `get`/`set` (backed by [parserOverride], a differently-named field) rather than a
     * plain `var parser: ... = defaultParser`: a plain var's default initializer compiles to an
     * unconditional `this.parser = defaultParser` in the constructor — since a plain var has no real
     * accessor on the prototype, that's a direct overwrite of whatever's already sitting in the
     * `parser` slot, silently destroying a value a caller assigned before this element was upgraded
     * to this real class (see [upgradeProperty]). Routing the default through the *getter* instead
     * means the constructor only ever touches [parserOverride]'s own (unreachable-from-outside) slot,
     * leaving a pre-upgrade `parser` value completely undisturbed for [upgradeProperty] to rescue.
     */
    var parser: (String) -> String
        get() = parserOverride ?: defaultParser
        set(value) {
            parserOverride = value
        }

    var markdown: String
        get() = getMarkdown()
        set(value) = renderMarkdown(value)

    private var toolbarField: Element? = null

    var toolbar: Element?
        get() = toolbarField
        set(value) {
            toolbarField = value
        }

    private var selectionRootField: org.w3c.dom.Document = document

    var selectionRoot: org.w3c.dom.Document
        get() = selectionRootField
        set(value) {
            selectionRootField = value
        }

    private var onLinkClickField: ((String) -> Unit)? = null

    var onLinkClick: ((String) -> Unit)?
        get() = onLinkClickField
        set(value) {
            onLinkClickField = value
        }

    private var autoNormalizeField: Boolean = true

    /**
     * When false, typing/editing no longer auto-normalizes on `input`. Flip this off from devtools
     * (`document.querySelector('markdown-document').autoNormalize = false`) to freeze a broken DOM
     * shape produced by a contenteditable quirk before it gets fixed away, then `copy(el.innerHTML)`
     * to grab it for a NormalizeCase regression test. Call `normalizeContent()` manually afterwards
     * to capture the expected "after" half of the fixture too.
     */
    var autoNormalize: Boolean
        get() = autoNormalizeField
        set(value) {
            autoNormalizeField = value
        }

    var currentSelection: Selection? = null
        private set

    var editable: Boolean = false
        private set

    private var lastAnchorNode: Node? = null
    private var lastAnchorOffset: Int = 0
    private var stashedSelection: StashedSelection? = null
    private var mouseSelection = false
    private val isChrome: Boolean = window.asDynamic().chrome != null

    private data class StashedSelection(val anchorNode: Node?, val anchorOffset: Int, val focusNode: Node?, val focusOffset: Int)

    private val onMouseDown: (Event) -> Unit = { event -> mousedown(event as MouseEvent) }
    private val onMouseUp: (Event) -> Unit = { mouseup() }
    private val onSelectStart: (Event) -> Unit = { selectstart() }
    private val onSelectionChange: (Event) -> Unit = { selectionchange() }

    init {
        // Must run after every property above has already been assigned its default (this init
        // block runs last among them, in declaration order) — upgradeProperty() re-assigns whichever
        // of these were set on this element *before* it became this class, and a default initializer
        // running afterwards would just clobber the rescued value right back to the default.
        upgradeProperty("parser")
        upgradeProperty("markdown")
        upgradeProperty("toolbar")
        upgradeProperty("selectionRoot")
        upgradeProperty("onLinkClick")
        upgradeProperty("autoNormalize")
    }

    protected fun renderStandardShadow() {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  display: block;
                  border: solid 1px gray;
                  border-top: none;
                  padding: 16px;
                  /* inherited by descendants (including a shadow-DOM toolbar/headings), matching
                     the original's separate styles/global-variables.ts stylesheet */
                  --header1-font-size: 2em;
                  --header2-font-size: 1.5em;
                  --header3-font-size: 1.17em;
                  --header4-font-size: 1em;
                  --header5-font-size: 0.83em;
                  --header6-font-size: 0.67em;
                }
                .toolbar {
                  z-index: 3;
                  top: 0px;
                  position: fixed;
                  right: 30%;
                  background: white;
                }
                .toc {
                  position: absolute;
                  z-index: 3;
                  top: 0px;
                  right: 0%;
                }
                @media print {
                  :host {
                    border: none;
                  }
                }
                """.trimIndent()
            )
            element("div") {
                element("div") { className = "toolbar"; slot("toolbar") }
                element("div") { className = "toc" }
                slot()
            }
        }
    }

    private var didFirstUpdate = false

    override fun connectedCallback() {
        super.connectedCallback()

        if (!didFirstUpdate) {
            didFirstUpdate = true
            firstUpdated()
        }

        setAttribute("contenteditable", if (getAttribute("readonly") == "true") "false" else "true")
        if (getAttribute("spellcheck") == null) {
            setAttribute("spellcheck", "false")
        }

        addEventListener("mousedown", onMouseDown)
        addEventListener("mouseup", onMouseUp)
        document.addEventListener("selectstart", onSelectStart)
        document.addEventListener("selectionchange", onSelectionChange)

        addEventListener("keydown", { event -> onKeyDown(event as KeyboardEvent) })
        addEventListener("input", { if (autoNormalize) normalizeContent(); onChange() })
        addEventListener("blur", { if (getAttribute("contenteditable") == "true") disableEditable() })
        addEventListener("drop", { event -> onDrop(event as DragEvent) })
        addEventListener("dragover", { event -> onDragOver(event as DragEvent) })
        addEventListener("paste", { event -> onPaste(event as ClipboardEvent) })
    }

    override fun disconnectedCallback() {
        document.removeEventListener("selectionchange", onSelectionChange)
        document.removeEventListener("selectstart", onSelectStart)
    }

    private fun onKeyDown(event: KeyboardEvent) {
        if (editable) {
            when (event.code) {
                "Enter" -> {
                    // Chromium stopped firing `input` here; insertLineBreak keeps it working.
                    event.preventDefault()
                    document.execCommand("insertLineBreak")
                }
                "Backspace" -> handleBackspaceKeyDown(event)
                "Delete" -> handleDeleteKeyDown(event)
                "Tab" -> {
                    event.preventDefault()
                    handleTabKeyDown(event.shiftKey)
                }
            }
            if (event.defaultPrevented) {
                // if the default was prevented, `input` likely won't fire on its own
                normalizeContent()
                onChange()
            }

            val current = getCurrentLeafBlock()
            // scrollIntoViewIfNeeded is non-standard (Chromium-only); calling it detached from
            // `current` (e.g. via a stored function reference) throws "Illegal invocation" — it
            // has to be invoked as current.scrollIntoViewIfNeeded() directly.
            if (current != null && current.asDynamic().scrollIntoViewIfNeeded != null) {
                current.asDynamic().scrollIntoViewIfNeeded()
            }
        }
    }

    private fun onDrop(event: DragEvent) {
        val files = event.dataTransfer?.files
        if (files != null && files.length == 1) {
            val file = files.item(0)!!
            if (file.type.matches(Regex("image.*"))) {
                event.stopPropagation()
                event.preventDefault()
                val reader = FileReader()
                reader.onload = {
                    val dataUri = reader.result as String
                    val img = document.createElement("markdown-image") as MarkdownImage
                    img.destination = dataUri

                    val pos = selectionRoot.caretPositionFromPoint(event.clientX.toDouble(), event.clientY.toDouble())
                    if (pos != null) {
                        val range = document.createRange()
                        range.setStart(pos.offsetNode, pos.offset)
                        range.collapse()
                        range.insertNode(img)
                    } else {
                        val range = selectionRoot.caretRangeFromPoint(event.clientX.toDouble(), event.clientY.toDouble())
                        if (range != null) {
                            range.insertNode(img)
                        } else if ((getSelection()?.rangeCount ?: 0) > 0) {
                            getSelection()?.getRangeAt(0)?.insertNode(img)
                        }
                    }
                }
                reader.readAsDataURL(file)
            }
        }
    }

    private fun onDragOver(event: DragEvent) {
        val pos = selectionRoot.caretPositionFromPoint(event.clientX.toDouble(), event.clientY.toDouble())
        if (pos != null) {
            val range = document.createRange()
            range.setStart(pos.offsetNode, pos.offset)
            range.collapse()
            getSelection()?.removeAllRanges()
            getSelection()?.addRange(range)
        } else {
            val range = selectionRoot.caretRangeFromPoint(event.clientX.toDouble(), event.clientY.toDouble())
            if (range != null) {
                getSelection()?.removeAllRanges()
                getSelection()?.addRange(range)
            }
        }
        event.preventDefault()
    }

    private fun onPaste(event: ClipboardEvent) {
        val pastedMarkdown = event.clipboardData?.getData("text/markdown")
        if (!pastedMarkdown.isNullOrEmpty()) {
            getSelection()?.deleteFromDocument()
            val range = getSelection()?.getRangeAt(0) ?: return
            val pastedNode = range.createContextualFragment(parser(pastedMarkdown))
            val firstChild = pastedNode.firstChild
            if (pastedNode.childNodes.length == 1 && firstChild is MarkdownComponent && firstChild.mustBeDirectChildOfDocument &&
                getSelection()?.anchorNode != this
            ) {
                // a top-level element was pasted: split up to the document root instead of nesting it
                var documentChild = getSelection()?.anchorNode
                while (documentChild != null && documentChild.parentElement != this) {
                    documentChild = documentChild.parentElement
                }
                if (documentChild is Element) {
                    val offset = getSelection()?.anchorOffset
                    val length = getSelection()?.anchorNode?.textContent?.length
                    if (offset != null && length != null && offset > length / 2) {
                        documentChild.after(pastedNode)
                    } else {
                        documentChild.before(pastedNode)
                    }
                } else {
                    range.insertNode(pastedNode)
                }
            } else {
                range.insertNode(pastedNode)
            }
            event.preventDefault()
            onChange()
        }
    }

    fun getSelection(): Selection? = selectionRoot.getSelection()

    private fun onMouseSelection() {
        // see https://bugs.chromium.org/p/chromium/issues/detail?id=1162730
        if (isChrome && getAttribute("contenteditable") == "true") {
            setAttribute("contenteditable", "false")
        }
    }

    private fun onEndMouseSelection() {
        if (isChrome && getAttribute("contenteditable") == "false") {
            setAttribute("contenteditable", if (getAttribute("readonly") == "true") "false" else "true")
            focus()
        }
    }

    private fun mousedown(event: MouseEvent) {
        if (event.buttons % 2 == 1) { // TODO: left-handed mouse button remapping?
            mouseSelection = true
        }
    }

    private fun mouseup() {
        mouseSelection = false
        onEndMouseSelection()
    }

    private fun selectstart() {
        // matches the original: intentionally empty (kept as a documented no-op hook)
    }

    private fun selectionchange() {
        val selection = getSelection()
        val anchorNode = selection?.anchorNode

        if (selection != null && anchorNode != null && contains(anchorNode)) {
            var element: Node? = anchorNode
            while (element != null && element !is MarkdownComponent) {
                element = element.parentNode
            }
            if (element is MarkdownComponent && element.isEditable()) {
                if (mouseSelection) onMouseSelection()
                enableEditable()
            } else {
                disableEditable()
            }

            currentSelection = selection
            lastAnchorNode = anchorNode
            lastAnchorOffset = selection.anchorOffset
            stashedSelection = StashedSelection(selection.anchorNode, selection.anchorOffset, selection.focusNode, selection.focusOffset)
            affectToolbar()
        } else {
            disableEditable()
        }
    }

    private fun enableEditable() {
        if (!editable) {
            editable = true
            toolbar?.classList?.add("focus-enabled")
            toolbar?.classList?.remove("focus-disabled")
        }
    }

    private fun disableEditable() {
        if (editable) {
            editable = false
            toolbar?.classList?.remove("focus-enabled")
            toolbar?.classList?.add("focus-disabled")
        }
    }

    /** A content range represents a selection independently of the actual DOM nodes/offsets involved. */
    private fun selectionToContentRange(): Pair<Int, Int>? {
        val selection = getSelection() ?: return null
        val anchorNode = selection.anchorNode ?: return null
        val focusNode = selection.focusNode ?: return null
        val anchorOffset = selectionNodeAndOffsetToContentOffset(anchorNode, selection.anchorOffset) ?: return null
        val focusOffset = selectionNodeAndOffsetToContentOffset(focusNode, selection.focusOffset) ?: return null
        return anchorOffset to focusOffset
    }

    private fun selectionNodeAndOffsetToContentOffset(node: Node, offset: Int): Int? {
        if (node == this) {
            return contentLengthUntil(childNodes.asList().getOrNull(offset))
        } else if (node is MarkdownComponent) {
            val parent = node.parentNode ?: return null
            val parentOffset = selectionNodeAndOffsetToContentOffset(parent, parent.childNodes.asList().indexOf(node))
            return parentOffset?.let { it + node.contentLengthUntil(node.childNodes.asList().getOrNull(offset)) }
        } else if (node is Text) {
            val parent = node.parentNode ?: return null
            val parentOffset = selectionNodeAndOffsetToContentOffset(parent, parent.childNodes.asList().indexOf(node))
            return parentOffset?.let {
                // count zero-width-space placeholders before `offset`: they aren't part of the real content
                val numberOfSpecialChars = (node.textContent?.take(offset)?.split(ZERO_WIDTH_SPACE)?.size ?: 1) - 1
                it + offset - numberOfSpecialChars
            }
        }
        return null
    }

    fun contentLengthUntil(child: Node?): Int {
        val nodes = childNodes.asList()
        val indexOfChild = if (child == null) -1 else nodes.indexOf(child)
        if (indexOfChild < 0) return 0
        var result = 0
        nodes.subList(0, indexOfChild).forEach { if (it is MarkdownComponent) result += it.contentLength() }
        return result
    }

    private fun setSelectionToContentRange(range: Pair<Int, Int>) {
        val (anchorNode, anchorOffset) = getNodeAndOffsetFromContentOffsetAnchor(range.first)
        val (focusNode, focusOffset) = getNodeAndOffsetFromContentOffset(range.second)
        val domRange = document.createRange()
        domRange.setStart(anchorNode, anchorOffset)
        domRange.setEnd(focusNode, focusOffset)
        currentSelection?.removeAllRanges()
        currentSelection?.addRange(domRange)
    }

    private fun getNodeAndOffsetFromContentOffsetAnchor(contentOffset: Int): Pair<Node, Int> {
        val elements = children.asList()
        if (elements.isEmpty()) return this to contentOffset

        var resultNode: Node = elements[0]
        for (child in elements) {
            val lengthUntilChild = contentLengthUntil(child)
            if (contentOffset == lengthUntilChild) {
                resultNode = child
                break
            }
            if (contentOffset == 0 || contentOffset < lengthUntilChild) break
            resultNode = child
        }
        return if (resultNode is MarkdownComponent) {
            resultNode.getNodeAndOffsetFromContentOffsetAnchor(contentOffset - contentLengthUntil(resultNode))
        } else {
            resultNode to (contentOffset - contentLengthUntil(resultNode))
        }
    }

    private fun getNodeAndOffsetFromContentOffset(contentOffset: Int): Pair<Node, Int> {
        val elements = children.asList()
        if (elements.isEmpty()) return this to contentOffset

        var resultNode: Node = elements[0]
        var previousNodeWasEol = false
        for (child in elements) {
            val lengthUntilChild = contentLengthUntil(child)
            if (contentOffset == lengthUntilChild) {
                if (previousNodeWasEol) resultNode = child
                break
            }
            if (contentOffset < lengthUntilChild) break
            resultNode = child
            previousNodeWasEol = child is MarkdownComponent && child.elementEndWithEndOfLineEquivalent()
        }
        return if (resultNode is MarkdownComponent) {
            resultNode.getNodeAndOffsetFromContentOffset(contentOffset - contentLengthUntil(resultNode))
        } else {
            resultNode to (contentOffset - contentLengthUntil(resultNode))
        }
    }

    fun normalizeContent() {
        domModificationOperation { normalizeDOM() }
    }

    private fun domModificationOperation(operation: () -> Unit) {
        val before = selectionToContentRange()
        operation()
        val after = selectionToContentRange()
        if (before != after && before != null) {
            setSelectionToContentRange(before)
        }
        affectToolbar()
    }

    private fun normalizeDOM() {
        for (child in childNodes.asList()) {
            when {
                child is MarkdownComponent -> {
                    if (child.normalizeContent()) {
                        normalizeDOM()
                        return
                    }
                }
                child is HTMLDivElement -> {
                    // Chromium represents new lines with <div>s on input; unwrap them.
                    child.childNodes.asList().toList().forEach { append(it) }
                    child.remove()
                }
                child is HTMLImageElement -> {
                    val img = document.createElement("markdown-image") as MarkdownImage
                    child.replaceWith(img)
                    child.getAttribute("src")?.let { img.destination = it }
                    child.getAttribute("title")?.let { img.destination = it } // matches the original (likely meant `title`)
                    child.getAttribute("alt")?.let { img.innerText = it }
                }
                child is Element && child.tagName.lowercase() == "markdown-paragraph" &&
                    child.childNodes.asList().singleOrNull() is HTMLBRElement -> {
                    (child.childNodes.asList().single() as HTMLBRElement).remove()
                    child.appendChild(document.createTextNode(ZERO_WIDTH_SPACE))
                }
                child is Text && child.textContent?.trim()?.isNotEmpty() == true -> {
                    val p = document.createElement("markdown-paragraph")
                    p.textContent = child.textContent
                    child.replaceWith(p)
                    (p as MarkdownComponent).normalizeContent()
                }
                else -> {
                    val html = document.createElement("markdown-html")
                    (child as ChildNode).replaceWith(html)
                    html.appendChild(child)
                }
            }
        }
        if (lastElementChild == null || lastElementChild?.tagName?.lowercase() != "markdown-paragraph") {
            val p = document.createElement("markdown-paragraph")
            p.textContent = ZERO_WIDTH_SPACE
            appendChild(p)
        }
    }

    fun contentLength(): Int {
        var result = 0
        children.asList().forEach { if (it is MarkdownComponent) result += it.contentLength() }
        return result
    }

    fun onChange() {
        window.setTimeout({ dispatchEvent(org.w3c.dom.CustomEvent("change")) }, 0)
    }

    fun setToolbar(newToolbar: Element) {
        toolbar = newToolbar
        (newToolbar as? MarkdownToolbar)?.setMarkdownDocument(this)
    }

    private fun firstUpdated() {
        if (getAttribute("floating-toc") == "true") {
            val toc = document.createElement("markdown-toc") as logic.tools.markdown.komponents.markdown.components.MarkdownToc
            toc.classList.add("floating")
            toc.markdownDocument = this
            shadowRoot?.querySelector(".toc")?.appendChild(toc)
        }
        if (getAttribute("toolbar") == "true") {
            val newToolbar = document.createElement("markdown-toolbar")
            shadowRoot?.querySelector(".toolbar")?.appendChild(newToolbar)
            setToolbar(newToolbar)
        }
    }

    private fun renderMarkdown(newMarkdown: String) {
        innerHTML = parser(newMarkdown)
        normalizeContent()
    }

    fun getMarkdown(): String = children.asList().joinToString("") { if (it is MarkdownComponent) it.getMarkdown() else "" }

    fun getCurrentLeafBlock(): logic.tools.markdown.komponents.markdown.LeafElement? {
        var element: Node? = getSelection()?.anchorNode
        while (element != null && element != document && element != this) {
            if (element is logic.tools.markdown.komponents.markdown.LeafElement) return element
            element = element.parentNode
        }
        return null
    }

    fun getLastLeafBlock(): logic.tools.markdown.komponents.markdown.LeafElement? {
        var element: Node? = lastAnchorNode
        while (element != null && element != document && element != this) {
            if (element is logic.tools.markdown.komponents.markdown.LeafElement) return element
            element = element.parentNode
        }
        return null
    }

    fun makeBreak() {
        val anchorOffset = currentSelection?.anchorOffset
        val focusOffset = currentSelection?.focusOffset
        val parent = currentSelection?.anchorNode?.parentElement
        if (parent != null && anchorOffset != null && focusOffset != null) {
            val replacementLeft = document.createElement("markdown-paragraph")
            val replacementRight = document.createElement("markdown-paragraph")
            val markdownBreak = document.createElement("markdown-break")

            replacementLeft.innerHTML = parent.innerHTML.slice(0 until anchorOffset)
            replacementRight.innerHTML = parent.innerHTML.slice(focusOffset until parent.innerHTML.length)
            if (replacementLeft.innerHTML.isEmpty()) replacementLeft.innerHTML = "<br />"
            if (replacementRight.innerHTML.isEmpty()) replacementRight.innerHTML = "<br />"

            parent.replaceWith(replacementLeft)
            replacementLeft.after(markdownBreak)
            markdownBreak.after(replacementRight)

            val range = document.createRange()
            range.selectNodeContents(replacementRight)
            range.collapse(true)
            currentSelection?.removeAllRanges()
            currentSelection?.addRange(range)

            dispatchEvent(org.w3c.dom.CustomEvent("markdown-inserted", org.w3c.dom.CustomEventInit(detail = markdownBreak)))
            onChange()
        }
    }

    private fun handleBackspaceKeyDown(event: KeyboardEvent) {
        val anchorOffset = currentSelection?.anchorOffset
        val focusOffset = currentSelection?.focusOffset
        val parent = currentSelection?.anchorNode?.parentElement
        val sibling = currentSelection?.anchorNode?.previousSibling
        if (parent != null && anchorOffset == 0 && focusOffset == 0 && parent is MarkdownComponent && sibling == null) {
            event.preventDefault()
            val grandparent = parent.parentElement
            if (grandparent is MarkdownElementEscapeByBackspace) {
                grandparent.escapeByBackspace(parent)
            } else {
                parent.mergeWithPrevious(currentSelection)
            }
        } else if (sibling != null && anchorOffset == 0 && focusOffset == 0 && sibling is MarkdownComponent && sibling.isDeletableAsAWhole()) {
            event.preventDefault()
            sibling.remove()
        }
    }

    private fun handleDeleteKeyDown(event: KeyboardEvent) {
        val anchor = currentSelection?.anchorNode
        val anchorOffset = currentSelection?.anchorOffset
        val focusOffset = currentSelection?.focusOffset
        val parent = anchor?.parentElement
        if (parent != null && anchor is Text && anchor.nextSibling == null &&
            anchorOffset == anchor.length && focusOffset == anchor.length && parent is MarkdownComponent
        ) {
            event.preventDefault()
            parent.mergeNextIn()
        }
    }

    private fun handleTabKeyDown(shift: Boolean) {
        var parent = currentSelection?.anchorNode?.parentElement
        var child: Element? = null
        while (parent != null && parent != this && parent !is MarkdownElementWithLevel) {
            child = parent
            parent = parent.parentElement
        }
        if (parent is MarkdownElementWithLevel) {
            if (shift) parent.goUpOneLevel(child) else parent.goDownOneLevel(child)
            onChange()
        }
    }

    private fun allRangeUnderInline(tagName: String): Boolean? {
        val selection = currentSelection
        return if (selection != null && selection.rangeCount > 0) allRangeUnderInline(tagName, selection.getRangeAt(0)) else null
    }

    private fun affectToolbar() {
        val toolbar = toolbar as? EditorToolbar
        if (allRangeUnderInline("markdown-strong") == true) toolbar?.highlightBoldButton() else toolbar?.removeBoldButtonHighlighting()
        if (allRangeUnderInline("markdown-emphasis") == true) toolbar?.highlightItalicButton() else toolbar?.removeItalicButtonHighlighting()
        if (allRangeUnderInline("markdown-strike") == true) toolbar?.highlightStrikeButton() else toolbar?.removeStrikeButtonHighlighting()

        val tagName = currentSelection?.anchorNode?.parentElement?.tagName
        when (tagName) {
            "MARKDOWN-PARAGRAPH" -> toolbar?.setDropdownTitle("Paragraph")
            "MARKDOWN-HEADER-1" -> toolbar?.setDropdownTitle("Heading 1")
            "MARKDOWN-HEADER-2" -> toolbar?.setDropdownTitle("Heading 2")
            "MARKDOWN-HEADER-3" -> toolbar?.setDropdownTitle("Heading 3")
            "MARKDOWN-HEADER-4" -> toolbar?.setDropdownTitle("Heading 4")
            "MARKDOWN-HEADER-5" -> toolbar?.setDropdownTitle("Heading 5")
            "MARKDOWN-HEADER-6" -> toolbar?.setDropdownTitle("Heading 6")
        }
    }

    fun makeBold() {
        domModificationOperation {
            currentSelection?.let { if (it.rangeCount > 0) surroundRangeIfNotYet("markdown-strong", it.getRangeAt(0)) }
            normalizeDOM()
        }
        onChange()
    }

    fun removeBold() {
        domModificationOperation {
            currentSelection?.let { if (it.rangeCount > 0) unsurroundRange("markdown-strong", it.getRangeAt(0)) }
            normalizeDOM()
        }
        onChange()
    }

    fun wrapCurrentSelectionInNewElement(elementName: String): Element? {
        val anchorOffset = currentSelection?.anchorOffset
        val focusOffset = currentSelection?.focusOffset
        val parent = currentSelection?.anchorNode?.parentElement
        if (parent == null || anchorOffset == null || focusOffset == null) return null

        val selectionLength = kotlin.math.abs(focusOffset - anchorOffset)
        // unlike TS's unchecked `as`, Kotlin's `as` really checks this at runtime — the anchor
        // isn't always a text node (e.g. the caret can sit right at an element boundary)
        val text = currentSelection?.anchorNode as? Text ?: return null
        val secondPart = text.splitText(minOf(anchorOffset, focusOffset))
        secondPart.splitText(selectionLength)
        val replacement = document.createElement(elementName)
        replacement.appendChild(document.createTextNode(secondPart.data))
        secondPart.replaceWith(replacement)

        val range = document.createRange()
        range.selectNodeContents(replacement)
        currentSelection?.removeAllRanges()
        currentSelection?.addRange(range)
        onChange()
        return replacement
    }

    fun makeItalic() {
        domModificationOperation {
            currentSelection?.let { if (it.rangeCount > 0) surroundRangeIfNotYet("markdown-emphasis", it.getRangeAt(0)) }
            normalizeDOM()
        }
        onChange()
    }

    fun removeItalic() {
        domModificationOperation {
            currentSelection?.let { if (it.rangeCount > 0) unsurroundRange("markdown-emphasis", it.getRangeAt(0)) }
            normalizeDOM()
        }
        onChange()
    }

    fun makeUnderline() {
        // not implemented in the original either
    }

    fun makeStrike() {
        domModificationOperation {
            currentSelection?.let { if (it.rangeCount > 0) surroundRangeIfNotYet("markdown-strike", it.getRangeAt(0)) }
            normalizeDOM()
        }
        onChange()
    }

    fun removeStrike() {
        domModificationOperation {
            currentSelection?.let { if (it.rangeCount > 0) unsurroundRange("markdown-strike", it.getRangeAt(0)) }
            normalizeDOM()
        }
        onChange()
    }

    fun makeCodeInline() {
        domModificationOperation {
            currentSelection?.let { if (it.rangeCount > 0) surroundRangeIfNotYet("markdown-code-span", it.getRangeAt(0)) }
            normalizeDOM()
        }
        onChange()
    }

    fun listBulletedClick() {
        val anchorNode = currentSelection?.anchorNode ?: return
        val list = document.createElement("markdown-list")
        val item = document.createElement("markdown-list-item")
        item.innerHTML = "<br />"
        list.appendChild(item)
        getCurrentLeafBlock()?.let { item.innerHTML = it.innerHTML }
        // the anchor is usually the text node under the caret, not an Element — same as the
        // surroundRangeIfNotYet fix, this needs ChildNode (which Text implements too)
        (anchorNode as ChildNode).replaceWith(list)

        val range = document.createRange()
        range.selectNodeContents(item)
        range.collapse(true)
        currentSelection?.removeAllRanges()
        currentSelection?.addRange(range)
        dispatchEvent(org.w3c.dom.CustomEvent("markdown-inserted", org.w3c.dom.CustomEventInit(detail = item)))
        onChange()
    }

    fun listNumericClick() {
        val anchorNode = currentSelection?.anchorNode ?: return
        val list = document.createElement("markdown-numeric-list")
        val item = document.createElement("markdown-numeric-list-item")
        item.innerHTML = "<br />"
        list.appendChild(item)
        getCurrentLeafBlock()?.let { item.innerHTML = it.innerHTML }
        // the anchor is usually the text node under the caret, not an Element — same as the
        // surroundRangeIfNotYet fix, this needs ChildNode (which Text implements too)
        (anchorNode as ChildNode).replaceWith(list)

        val range = document.createRange()
        range.selectNodeContents(item)
        range.collapse(true)
        currentSelection?.removeAllRanges()
        currentSelection?.addRange(range)
        dispatchEvent(org.w3c.dom.CustomEvent("markdown-inserted", org.w3c.dom.CustomEventInit(detail = item)))
        onChange()
    }

    fun insertPhoto(url: String?, text: String?) {
        val anchorNode = currentSelection?.anchorNode ?: return
        val image = document.createElement("markdown-image") as MarkdownImage
        if (text != null) image.title = text
        if (url != null) image.destination = url

        val anchorOffset = currentSelection?.anchorOffset
        val text2 = anchorNode as? Text
        if (anchorOffset != null && text2 != null) {
            text2.splitText(anchorOffset)
            text2.after(image)
            dispatchEvent(org.w3c.dom.CustomEvent("markdown-inserted", org.w3c.dom.CustomEventInit(detail = image)))
            onChange()
        }
    }

    fun restoreStashedSelection() {
        val stashed = stashedSelection ?: return
        val range = document.createRange()
        range.setStart(stashed.anchorNode!!, stashed.anchorOffset)
        range.setEnd(stashed.focusNode!!, stashed.focusOffset)
        currentSelection?.removeAllRanges()
        currentSelection?.addRange(range)
    }

    fun insertLink() {
        val link = wrapCurrentSelectionInNewElement("markdown-link") as? MarkdownLink ?: return
        if (link.textContent.isNullOrEmpty()) {
            link.textContent = "http://"
        }
        link.destination = link.textContent ?: ""
        link.classList.add("fresh")
    }

    fun header1Element() = replaceCurrentLeafBlockWith("markdown-header-1")
    fun header2Element() = replaceCurrentLeafBlockWith("markdown-header-2")
    fun header3Element() = replaceCurrentLeafBlockWith("markdown-header-3")
    fun header4Element() = replaceCurrentLeafBlockWith("markdown-header-4")
    fun header5Element() = replaceCurrentLeafBlockWith("markdown-header-5")
    fun header6Element() = replaceCurrentLeafBlockWith("markdown-header-6")
    fun pararaphElement() = replaceCurrentLeafBlockWith("markdown-paragraph")

    private fun replaceCurrentLeafBlockWith(tagName: String) {
        val oldElement = getCurrentLeafBlock() ?: return
        val element = document.createElement(tagName)
        element.innerHTML = oldElement.innerHTML
        oldElement.replaceWith(element)
        onChange()
    }

    fun makeCodeBlock() {
        val oldElement = getCurrentLeafBlock() ?: return
        val element = document.createElement("markdown-code")
        element.innerHTML = oldElement.innerHTML
        oldElement.replaceWith(element)
        dispatchEvent(org.w3c.dom.CustomEvent("markdown-inserted", org.w3c.dom.CustomEventInit(detail = element)))
        onChange()
    }

    fun makeQuoteBlock() {
        val oldElement = getCurrentLeafBlock() ?: return
        val element = document.createElement("markdown-quote")
        val p = document.createElement("markdown-paragraph")
        element.appendChild(p)
        p.innerHTML = oldElement.innerHTML
        oldElement.replaceWith(element)
        dispatchEvent(org.w3c.dom.CustomEvent("markdown-inserted", org.w3c.dom.CustomEventInit(detail = element)))
        onChange()
    }
}

private fun getWord(range: Range): Range? {
    val container = range.commonAncestorContainer
    if (range.collapsed && container is Text) {
        val text = container.textContent ?: ""
        val leftIndex = maxOf(0, text.substring(0, range.startOffset).lastIndexOf(' '))
        var rightIndex = text.substring(range.startOffset).indexOf(' ') + range.startOffset
        if (rightIndex <= range.startOffset) rightIndex = text.length
        val result = Range()
        result.setStart(range.startContainer, leftIndex)
        result.setEnd(range.startContainer, rightIndex)
        return result
    }
    return null
}

private fun allRangeUnderInline(tagName: String, range: Range): Boolean {
    val container = range.commonAncestorContainer
    if (container is Text) {
        return container.parentElement?.closest(tagName) != null
    } else if (container is Element && container.closest(tagName) != null) {
        return true
    }
    val walker = document.createTreeWalker(container, org.w3c.dom.NodeFilter.SHOW_TEXT, null as org.w3c.dom.NodeFilter?)
    var result = false
    var node = walker.nextNode()
    while (node != null) {
        if (node is Text && range.intersectsNode(node) && isMarkdownContentTextNode(node)) {
            if (node.parentElement?.closest(tagName) == null) return false
            result = true
        }
        node = walker.nextNode()
    }
    return result
}

private fun getAllTextNodesForRange(range: Range): List<Text> {
    val startNode = range.startContainer
    val endNode = range.endContainer
    val startOffset = range.startOffset
    var endOffset = range.endOffset

    if (startNode is Text && startOffset > 0 && startOffset < (startNode.textContent?.length ?: 0)) {
        val text = startNode.textContent ?: ""
        startNode.parentElement?.insertBefore(document.createTextNode(text.substring(0, startOffset)), startNode)
        startNode.textContent = text.substring(startOffset)
        range.setStart(startNode, 0)
        if (startNode == endNode) endOffset -= startOffset
    }
    if (endNode is Text && endOffset < (endNode.textContent?.length ?: 0)) {
        val text = endNode.textContent ?: ""
        val after = document.createTextNode(text.substring(endOffset))
        endNode.textContent = text.substring(0, endOffset)
        endNode.after(after)
    }

    val container = range.commonAncestorContainer
    if (container is Text) return listOf(container)

    val result = mutableListOf<Text>()
    val walker = document.createTreeWalker(container, org.w3c.dom.NodeFilter.SHOW_TEXT, null as org.w3c.dom.NodeFilter?)
    var node = walker.nextNode()
    while (node != null) {
        if (node is Text && range.intersectsNode(node) && isMarkdownContentTextNode(node)) {
            result.add(node)
        }
        node = walker.nextNode()
    }
    return result
}

private fun isMarkdownContentTextNode(node: Text): Boolean {
    val parent = node.parentNode
    return parent is MarkdownComponent && parent.containsMarkdownTextContent()
}

fun surroundRangeIfNotYet(tagName: String, range: Range) {
    if (range.collapsed) {
        val wordRange = getWord(range)
        if (wordRange != null) {
            surroundRangeIfNotYet(tagName, wordRange)
        } else {
            // TODO: do this ourselves based on the range instead of execCommand
            document.execCommand("insertHTML", false, "<$tagName>&ZeroWidthSpace;</$tagName>")
        }
        return
    }
    getAllTextNodesForRange(range).forEach { text ->
        if (text.parentElement?.closest(tagName) == null) {
            var replaceLevel: Node = text
            while (replaceLevel.parentElement is logic.tools.markdown.komponents.markdown.TerminalInlineElement) {
                replaceLevel = replaceLevel.parentElement!!
            }
            val enclosing = document.createElement(tagName)
            // replaceLevel is the text node itself unless promoted to an ancestor above — it's
            // never necessarily an Element, just something that implements ChildNode (as Text does too)
            (replaceLevel as ChildNode).replaceWith(enclosing)
            enclosing.appendChild(replaceLevel)
        }
    }
}

private fun dispatchToChildren(element: Element) {
    val sibling = element.nextSibling
    val parent = element.parentElement
    element.remove()
    element.childNodes.asList().toList().forEach { child ->
        val clone = element.cloneNode(false) as Element
        clone.appendChild(child)
        parent?.insertBefore(clone, sibling)
    }
}

fun unsurroundRange(tagName: String, range: Range) {
    if (range.collapsed) {
        val wordRange = getWord(range)
        if (wordRange != null) unsurroundRange(tagName, wordRange)
        return
    }
    val allTexts = getAllTextNodesForRange(range)
    var surrounded: Boolean
    do {
        surrounded = false
        allTexts.forEach { text ->
            var enclosing = text.parentElement?.closest(tagName)
            if (enclosing != null) {
                surrounded = true
                dispatchToChildren(enclosing)
                if (text.parentElement?.tagName?.lowercase() == tagName) {
                    text.parentElement?.replaceWith(text)
                } else {
                    enclosing = text.parentElement?.closest(tagName)!!
                    val child = enclosing.childNodes.asList()[0] // dispatchToChildren left exactly one
                    enclosing.replaceWith(child)
                    child.childNodes.asList().toList().forEach { grandchild ->
                        child.removeChild(grandchild)
                        enclosing.appendChild(grandchild)
                    }
                    child.appendChild(enclosing)
                }
            }
        }
    } while (surrounded)
}

@JsExport
class MarkdownDocument : BaseMarkdownDocument() {
    init {
        renderStandardShadow()
    }
}
