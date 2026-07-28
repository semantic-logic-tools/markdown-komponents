@file:OptIn(ExperimentalJsExport::class)

package logic.tools.markdown.komponents.markdown.components

import logic.tools.markdown.komponents.element
import logic.tools.markdown.komponents.markdown.TerminalInlineElement
import org.w3c.dom.CustomEvent
import org.w3c.dom.CustomEventInit
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import org.w3c.files.FileReader
import logic.tools.markdown.komponents.style

/** Open so consumers can subclass it to customize how an image renders, e.g. inlining a data URI differently. */
@JsExport
open class MarkdownImage : TerminalInlineElement() {
    override val mustBeDirectChildOfDocument = false

    protected val img: HTMLImageElement
    protected val uploadInput: HTMLInputElement

    var destination: String
        get() = getAttribute("destination") ?: ""
        set(value) = setAttribute("destination", value)

    // `title` is inherited from HTMLElement itself (a native attribute-backed property already).

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        shadow.style(
            """
            :host([destination]) .upload {
              display: none;
            }
            .upload {
              border: #c7c7c7 1px dashed;
              padding: 10px;
            }
            """.trimIndent()
        )
        uploadInput = shadow.element("input") {
            className = "upload"
            setAttribute("type", "file")
            setAttribute("accept", "image/*")
        } as HTMLInputElement
        uploadInput.addEventListener("change", { upload() })
        img = shadow.element("img") { setAttribute("part", "image") } as HTMLImageElement
        img.addEventListener("error", { onError() })
        shadow.element("slot") { setAttribute("style", "display:none;") } // the alt text source, not shown directly
    }

    override fun connectedCallback() {
        super.connectedCallback()
        render()
    }

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        if (name == "destination" || name == "title") {
            render()
        }
    }

    /** Updates the shadow DOM to reflect current state. Override to customize how the image is displayed. */
    protected open fun render() {
        img.src = destination
        img.title = title
        img.alt = innerText
    }

    private fun onError() {
        dispatchEvent(CustomEvent("error-image", CustomEventInit(detail = destination, bubbles = true, cancelable = true)))
    }

    private fun upload() {
        val file = uploadInput.files?.item(0) ?: return
        val reader = FileReader()
        reader.onload = {
            setAttribute("destination", reader.result as String)
        }
        reader.readAsDataURL(file)
    }

    fun setImageSrc(src: String) {
        img.src = src
    }

    override fun getMarkdown(): String = "![$innerText]($destination \"$title\")"

    override fun containsMarkdownTextContent(): Boolean = false

    override fun isDeletableAsAWhole(): Boolean = true
}
