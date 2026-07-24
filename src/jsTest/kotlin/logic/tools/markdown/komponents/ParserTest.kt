package logic.tools.markdown.komponents

import logic.tools.markdown.komponents.editor.MarkdownDocument
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.asList
import logic.tools.markdown.komponents.tsstack.parseMarkdown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises `markdown-document`'s `parser` property end-to-end using the optional `tsstack`
 * package's [parseMarkdown] — one ready-made way to wire up `parser`, not a bundled default:
 * `parser` itself stays a plain `(String) -> String` the consumer supplies.
 */
class ParserTest {

    @Test
    fun parsesRepresentativeMarkdownIntoExpectedComponents() {
        val doc = createElement("markdown-document") as MarkdownDocument
        doc.parser = ::parseMarkdown
        document.body?.appendChild(doc)

        try {
            doc.markdown = """
                |# Introduction
                |
                |## Details {custom-id}
                |
                |A paragraph with **bold**, *italic*, ~~strike~~, `code`, a [link](https://kotlinlang.org) and an image ![alt](https://example.com/x.png).
                |
                |---
                |
                |> A quoted paragraph.
                |
                |- Bullet one
                |- [ ] Open task
                |- [x] Done task
                |
                |Numbered list below.
                |
                |1. Numbered one
                |2. Numbered two
                |
                || Column A | Column B |
                || --- | --- |
                || 1 | 2 |
                |
                |```kotlin
                |fun main() = println("hi")
                |```
                |""".trimMargin()

            val h1 = doc.querySelector("markdown-header-1") ?: error("expected an h1, got:\n${doc.innerHTML}")
            assertEquals("Introduction", h1.textContent)
            assertEquals("heading-introduction", h1.getAttribute("id"))

            val h2 = doc.querySelector("markdown-header-2") ?: error("expected an h2, got:\n${doc.innerHTML}")
            assertEquals("Details", h2.textContent)
            assertEquals("custom-id", h2.getAttribute("id"))

            val paragraph = doc.querySelector("markdown-paragraph") ?: error("expected a paragraph, got:\n${doc.innerHTML}")
            assertEquals("bold", paragraph.querySelector("markdown-strong")?.textContent)
            assertEquals("italic", paragraph.querySelector("markdown-emphasis")?.textContent)
            assertEquals("strike", paragraph.querySelector("markdown-strike")?.textContent)
            assertEquals("code", paragraph.querySelector("markdown-code-span")?.textContent)
            val link = paragraph.querySelector("markdown-link")
            assertEquals("https://kotlinlang.org", link?.getAttribute("destination"))
            assertEquals("link", link?.textContent)
            val image = paragraph.querySelector("markdown-image")
            assertEquals("https://example.com/x.png", image?.getAttribute("destination"))
            assertEquals("alt", image?.textContent)

            assertTrue(doc.querySelector("markdown-break") != null, "expected a hr, got:\n${doc.innerHTML}")

            val quote = doc.querySelector("markdown-quote") ?: error("expected a blockquote, got:\n${doc.innerHTML}")
            assertEquals("A quoted paragraph.", quote.querySelector("markdown-paragraph")?.textContent)

            val lists = doc.querySelectorAll("markdown-list").asList().filterIsInstance<Element>()
            assertEquals(2, lists.size, "expected a bulleted and a numbered list, got:\n${doc.innerHTML}")
            val (bulletedList, numberedList) = lists

            assertEquals("false", bulletedList.getAttribute("ordered"))
            val plainItems = bulletedList.querySelectorAll("markdown-list-item").asList().filterIsInstance<Element>()
            assertEquals(1, plainItems.size, "expected only the plain bullet as a plain list item, got:\n${doc.innerHTML}")
            assertEquals("Bullet one", plainItems[0].textContent)

            val tasks = bulletedList.querySelectorAll("markdown-task-list-item").asList().filterIsInstance<Element>()
            assertEquals(2, tasks.size)
            assertEquals("Open task", tasks[0].textContent)
            assertTrue(tasks[0].getAttribute("checked") != "true")
            assertEquals("Done task", tasks[1].textContent)
            assertEquals("true", tasks[1].getAttribute("checked"))

            assertEquals("true", numberedList.getAttribute("ordered"))
            val numberedItems = numberedList.querySelectorAll("markdown-list-item").asList().filterIsInstance<Element>()
            assertEquals(2, numberedItems.size)
            assertEquals("Numbered one", numberedItems[0].textContent)
            assertEquals("Numbered two", numberedItems[1].textContent)

            val table = doc.querySelector("markdown-table") ?: error("expected a table, got:\n${doc.innerHTML}")
            val headerCells = table.querySelectorAll("markdown-table-header-cell").asList().filterIsInstance<Element>()
            assertEquals(listOf("Column A", "Column B"), headerCells.map { it.textContent })
            val bodyCells = table.querySelectorAll("markdown-table-cell").asList().filterIsInstance<Element>()
            assertEquals(listOf("1", "2"), bodyCells.map { it.textContent })

            val code = doc.querySelector("markdown-code") ?: error("expected a fenced code block, got:\n${doc.innerHTML}")
            assertEquals("kotlin", code.getAttribute("lang"))
            assertEquals("fun main() = println(\"hi\")", code.textContent?.trim())

            assertTrue(doc.getMarkdown().isNotBlank(), "expected non-blank markdown output")
        } finally {
            document.body?.removeChild(doc)
        }
    }
}
