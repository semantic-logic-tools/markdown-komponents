package logic.tools.markdown.komponents

import logic.tools.markdown.komponents.editor.BaseMarkdownDocument
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.test.Test
import kotlin.test.assertTrue

// A given class can only be registered under one tag name across the whole CustomElementRegistry
// (a second customElements.define() call for the same class throws NotSupportedError) — this test
// needs a tag name of its own, so it needs a class of its own too, rather than reusing MarkdownDocument
// (already registered as `markdown-document` by AutoRegister.kt).
private class TestRescueMarkdownDocument : BaseMarkdownDocument() {
    init {
        renderStandardShadow()
    }
}

private class TestRescueMarkdownDocument2 : BaseMarkdownDocument() {
    init {
        renderStandardShadow()
    }
}

/**
 * Reproduces the exact browser mechanism behind a real bug found integrating this into a consumer:
 * code setting a property on a `<markdown-document>`-tagged element *before* the browser has
 * upgraded it to this real class (e.g. it's still sitting in an inert `<template>`, or in a shadow
 * DOM built by a component's own constructor while that component is itself still disconnected — see
 * `BaseWebComponent.upgradeProperty`'s doc comment) lands as a plain own value with no accessor
 * behind it. Once upgraded, that own value permanently shadows this class's real accessor unless
 * something rescues it — the original TS library extended Lit's `ReactiveElement`, which does this
 * automatically; this one has to do it itself (`upgradeProperty`, called from the constructor).
 *
 * This test drives the real upgrade sequence — an actually-undefined custom element, connected to
 * the document, then defined — rather than simulating it, so it fails for real if the rescue logic
 * ever regresses.
 */
class UpgradePropertyTest {

    @Test
    fun rescuesAPropertySetBeforeThisElementWasUpgraded() {
        val tagName = "markdown-document-rescue-test"

        // Not yet defined: `notYetUpgraded` starts life as a plain, undefined custom element — its
        // prototype has no `markdown` accessor at all yet, exactly like a browser-parsed element
        // sitting in the DOM before this library's module has registered the real tag.
        val notYetUpgraded = document.createElement(tagName)
        notYetUpgraded.asDynamic().markdown = "# Rescued heading"
        document.body?.appendChild(notYetUpgraded)

        try {
            // Defining the tag now upgrades every matching, already-connected element in the
            // document in place (same object, new prototype) — constructing it (running
            // BaseMarkdownDocument's upgradeProperty rescue) with the pre-existing own `markdown`
            // value still intact, exactly as the real upgrade race does.
            window.customElements.define(tagName, TestRescueMarkdownDocument::class.js)

            val doc = notYetUpgraded as TestRescueMarkdownDocument
            // If the rescue worked, the pending value was driven back through the real setter
            // (renderMarkdown -> innerHTML = parser(...)) instead of being left sitting inert.
            assertTrue(
                doc.querySelector("markdown-paragraph") != null,
                "expected the pre-upgrade markdown value to have been parsed, got:\n${doc.innerHTML}",
            )
        } finally {
            document.body?.removeChild(notYetUpgraded)
        }
    }

    /**
     * `parser` (and `toolbar`/`selectionRoot`/`onLinkClick`/`autoNormalize`) had a subtler version of
     * the same bug: as a plain `var parser: ... = defaultParser`, the class's *own* default-value
     * initializer compiled to an unconditional `this.parser = defaultParser` in the constructor —
     * which ran *before* `upgradeProperty('parser')`, silently overwriting a pre-upgrade value with
     * the placeholder before the rescue ever saw it. `parser` needed an explicit `get`/`set` (backed
     * by a differently-named field) so the default only ever touches that private field, never the
     * public `parser` slot a pre-upgrade caller wrote to.
     */
    @Test
    fun rescuesParserSetBeforeThisElementWasUpgraded() {
        val tagName = "markdown-document-rescue-parser-test"

        val notYetUpgraded = document.createElement(tagName)
        val customParser: (String) -> String = { md -> "<markdown-header-1>$md</markdown-header-1>" }
        notYetUpgraded.asDynamic().parser = customParser
        notYetUpgraded.asDynamic().markdown = "custom-parsed"
        document.body?.appendChild(notYetUpgraded)

        try {
            window.customElements.define(tagName, TestRescueMarkdownDocument2::class.js)

            val doc = notYetUpgraded as TestRescueMarkdownDocument2
            assertTrue(
                doc.querySelector("markdown-header-1") != null,
                "expected the pre-upgrade custom parser to have been used (not the default placeholder), got:\n${doc.innerHTML}",
            )
        } finally {
            document.body?.removeChild(notYetUpgraded)
        }
    }
}
