package logic.tools.markdown.komponents

import logic.tools.markdown.komponents.editor.BaseMarkdownDocument
import kotlinx.browser.document
import logic.tools.markdown.komponents.markdown.MarkdownComponent
import kotlin.test.Test
import kotlin.test.fail

/** Re-parses HTML through the browser once so trivial formatting differences don't cause false failures. */
private fun canonicalize(html: String): String {
    val div = document.createElement("div")
    div.innerHTML = html
    return div.innerHTML
}

private fun runNormalize(case: NormalizeCase): String {
    val container = document.createElement(case.containerTag)
    container.innerHTML = case.before
    when (container) {
        is BaseMarkdownDocument -> container.normalizeContent()
        is MarkdownComponent -> container.normalizeContent()
        else -> error("Don't know how to normalize a <${case.containerTag}> — it's neither a markdown-document nor a markdown component")
    }
    return container.innerHTML
}

class NormalizeRegressionTest {
    @Test
    fun allCasesNormalizeAsExpected() {
        // no explicit registration needed: importing this module (jsTest depends on jsMain)
        // already registered every custom element via AutoRegister.kt's eager init
        val failures = mutableListOf<String>()
        for (case in normalizeCases) {
            val expected = canonicalize(case.after)
            val actual = try {
                runNormalize(case)
            } catch (e: Throwable) {
                failures.add("\"${case.name}\" threw ${e}")
                continue
            }
            if (actual != expected) {
                failures.add("\"${case.name}\"\n    expected: $expected\n    actual:   $actual")
            }
        }

        if (failures.isNotEmpty()) {
            fail("${failures.size} of ${normalizeCases.size} normalize case(s) failed:\n\n" + failures.joinToString("\n\n"))
        }
    }
}
