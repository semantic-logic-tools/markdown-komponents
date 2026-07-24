package logic.tools.markdown.komponents

import logic.tools.markdown.komponents.editor.registerEditorComponents
import logic.tools.markdown.komponents.markdown.components.registerMarkdownComponents

/**
 * Registers every custom element this library provides, as a side effect of the module being
 * loaded. `@EagerInitialization` forces this to run at module-evaluation time regardless of
 * binary kind (executable or library) — so `import '@you/markdown-komponents'` alone is enough
 * for a consumer; nothing needs to be called explicitly.
 */
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
private val registerAllComponentsOnLoad = run {
    registerMarkdownComponents()
    registerEditorComponents()
}
