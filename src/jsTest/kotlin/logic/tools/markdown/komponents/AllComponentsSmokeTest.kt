package logic.tools.markdown.komponents

import logic.tools.markdown.komponents.editor.MarkdownDocument
import kotlinx.browser.document
import logic.tools.markdown.komponents.markdown.components.MarkdownToc
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Builds one of every component type inside a real, connected markdown-document (toolbar
 * included) and checks it constructs, normalizes and serializes back to markdown without
 * throwing. Not a substitute for NormalizeRegressionTest's exact before/after fixtures — this is
 * a broad "did anything blow up" sweep across the whole component set.
 */
class AllComponentsSmokeTest {

    @Test
    fun buildsEditsAndSerializesEveryComponentTypeWithoutThrowing() {
        // no explicit registration needed: importing this module (jsTest depends on jsMain)
        // already registered every custom element via AutoRegister.kt's eager init
        val doc = createElement("markdown-document") as MarkdownDocument
        doc.setAttribute("toolbar", "true")

        doc.appendChild(createElement("markdown-header-1") { id = "h1"; textContent = "Heading 1" })
        doc.appendChild(createElement("markdown-header-2") { id = "h2"; textContent = "Heading 2" })
        doc.appendChild(createElement("markdown-header-3") { textContent = "Heading 3" })
        doc.appendChild(createElement("markdown-header-4") { textContent = "Heading 4" })
        doc.appendChild(createElement("markdown-header-5") { textContent = "Heading 5" })
        doc.appendChild(createElement("markdown-header-6") { textContent = "Heading 6" })

        doc.appendChild(
            createElement("markdown-paragraph") {
                appendChild(document.createTextNode("A paragraph with "))
                appendChild(createElement("markdown-strong") { textContent = "bold" })
                appendChild(document.createTextNode(", "))
                appendChild(createElement("markdown-emphasis") { textContent = "italic" })
                appendChild(document.createTextNode(", "))
                appendChild(createElement("markdown-strike") { textContent = "strikethrough" })
                appendChild(document.createTextNode(", "))
                appendChild(createElement("markdown-code-span") { textContent = "inline code" })
                appendChild(document.createTextNode(", a "))
                appendChild(
                    createElement("markdown-link") {
                        setAttribute("destination", "https://kotlinlang.org")
                        textContent = "link"
                    }
                )
                appendChild(document.createTextNode(" and an image "))
                appendChild(createElement("markdown-image") { setAttribute("destination", "") })
                appendChild(document.createTextNode(" all together."))
            }
        )

        doc.appendChild(createElement("markdown-break"))

        doc.appendChild(
            createElement("markdown-quote") {
                appendChild(createElement("markdown-paragraph") { textContent = "A quoted paragraph, for testing backspace-to-escape and Tab nesting." })
            }
        )

        doc.appendChild(
            createElement("markdown-list") {
                appendChild(createElement("markdown-list-item") { textContent = "Bullet item one" })
                appendChild(createElement("markdown-list-item") { textContent = "Bullet item two" })
                appendChild(createElement("markdown-task-list-item") {
                    setAttribute("checked", "true")
                    textContent = "Completed task"
                })
                appendChild(createElement("markdown-task-list-item") { textContent = "Open task" })
            }
        )

        doc.appendChild(
            createElement("markdown-numeric-list") {
                appendChild(createElement("markdown-numeric-list-item") { textContent = "Numbered item one" })
                appendChild(createElement("markdown-numeric-list-item") { textContent = "Numbered item two" })
            }
        )

        doc.appendChild(
            createElement("markdown-table") {
                appendChild(
                    createElement("markdown-table-header-row") {
                        appendChild(createElement("markdown-table-header-cell") { textContent = "Column A" })
                        appendChild(createElement("markdown-table-header-cell") { textContent = "Column B" })
                    }
                )
                appendChild(
                    createElement("markdown-table-row") {
                        appendChild(createElement("markdown-table-cell") { textContent = "1" })
                        appendChild(createElement("markdown-table-cell") { textContent = "2" })
                    }
                )
            }
        )

        doc.appendChild(
            createElement("markdown-code") {
                setAttribute("lang", "kotlin")
                textContent = "fun main() = println(\"hi\")"
            }
        )

        doc.appendChild(createElement("markdown-html") { innerHTML = "<em>raw html</em> content" })

        document.body?.appendChild(doc)
        doc.normalizeContent()

        val toc = createElement("markdown-toc") as MarkdownToc
        document.body?.prepend(toc)
        toc.markdownDocument = doc

        val markdown = doc.getMarkdown()
        assertTrue(markdown.isNotBlank(), "expected non-blank markdown output")
        assertTrue(markdown.contains("**bold**"), "expected bold markup, got:\n$markdown")
        assertTrue(markdown.contains("*italic*"), "expected italic markup, got:\n$markdown")
        assertTrue(markdown.contains("~~strikethrough~~"), "expected strikethrough markup, got:\n$markdown")
        assertTrue(markdown.contains("```"), "expected a fenced code block, got:\n$markdown")
        assertTrue(markdown.contains("[link](https://kotlinlang.org)"), "expected link markup, got:\n$markdown")

        document.body?.removeChild(toc)
        document.body?.removeChild(doc)
    }
}
