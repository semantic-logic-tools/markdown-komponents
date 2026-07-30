package logic.tools.markdown.komponents

import kotlinx.browser.document
import kotlinx.browser.window
import logic.tools.markdown.komponents.editor.MarkdownDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Stand-in for a consumer-defined custom element implementing
 * `MarkdownElement`'s shape structurally — the whole point being that it does NOT extend this
 * library's `MarkdownComponent`, so `is MarkdownComponent` can't see it, exactly like a plain
 * TypeScript class wouldn't.
 */
private class FakeConsumerCodeEditor : BaseWebComponent() {
    // A plain TypeScript class's methods are never name-mangled the way an un-exported Kotlin
    // class's are — @JsName pins this method's compiled name to match, so the duck-typed check in
    // asMarkdownElement() actually finds it, faithfully reproducing what a real consumer looks like.
    @JsName("getMarkdown")
    fun getMarkdown(): String = "```fake\ncode\n```"
}

/**
 * Reproduces a real bug found integrating with a consumer: a consumer-defined element implementing
 * `MarkdownElement` structurally, placed as a direct child of `markdown-document`, was silently
 * wrapped in `<markdown-html>` the moment `normalizeDOM()` ran (e.g. on the very next `input` event)
 * — its catch-all branch for anything that isn't a recognized `MarkdownComponent`/`Div`/`Image`/etc.
 * `getMarkdown()` on the document then serialized that wrapper's raw `innerHTML` instead of ever
 * calling the consumer element's own `getMarkdown()`.
 */
class ConsumerMarkdownElementTest {

    @Test
    fun doesNotWrapAConsumerDefinedMarkdownElementAndCallsItsGetMarkdown() {
        window.customElements.define("fake-consumer-code-editor", FakeConsumerCodeEditor::class.js)

        val doc = createElement("markdown-document") as MarkdownDocument
        document.body?.appendChild(doc)
        try {
            val codeEditor = document.createElement("fake-consumer-code-editor")
            doc.appendChild(codeEditor)

            doc.normalizeContent()

            assertNull(
                doc.querySelector("markdown-html"),
                "expected the consumer element not to be wrapped, got:\n${doc.innerHTML}",
            )
            assertEquals("```fake\ncode\n```", doc.getMarkdown().trim())
        } finally {
            document.body?.removeChild(doc)
        }
    }
}
