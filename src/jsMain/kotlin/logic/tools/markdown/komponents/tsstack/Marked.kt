package logic.tools.markdown.komponents.tsstack

/**
 * Native JS `RegExp`, needed for [Marked.setBlockRule]: the Kotlin stdlib's `Regex` wraps a
 * native pattern internally but doesn't expose it, and this library's API is built directly
 * around `RegExp.exec()`-shaped results (a match array with a capture group per index).
 */
@JsName("RegExp")
external class JsRegExp(pattern: String, flags: String = definedExternally) {
    fun exec(input: String): Array<String?>?
}

/**
 * A single `@JsModule`-annotated `external object` imports the whole `@ts-stack/markdown` module
 * as a namespace (`import * as TsStackMarkdown from '@ts-stack/markdown'`); its members below map
 * 1:1 onto that module's actual named exports (`MarkedOptions`, `Marked`, `Renderer`). Binding
 * each of those individually via its own top-level `@JsModule` doesn't work here — the package
 * has no default export, which is what a bare `@JsModule` on a single class/object imports.
 */
@JsModule("@ts-stack/markdown")
external object TsStackMarkdown {

    /** Mirrors `MarkedOptions` (see `src/interfaces.d.ts` in the package). */
    class MarkedOptions {
        var gfm: Boolean?
        var tables: Boolean?
        var breaks: Boolean?
        var pedantic: Boolean?
        var sanitize: Boolean?
        var mangle: Boolean?
        var smartLists: Boolean?
        var silent: Boolean?
        var langPrefix: String?
        var smartypants: Boolean?
        var headerPrefix: String?
        var renderer: Renderer?
        var xhtml: Boolean?
        var isNoP: Boolean?
        var escape: ((html: String, encode: Boolean) -> String)?
        var unescape: ((html: String) -> String)?
    }

    /** Mirrors the `Marked` static entry point (see `src/marked.d.ts`). */
    object Marked {
        fun setBlockRule(regexp: JsRegExp, renderer: (Array<String?>?) -> String = definedExternally): dynamic
        fun parse(src: String, options: MarkedOptions = definedExternally): String
    }

    /**
     * Mirrors the `Renderer` base class (see `src/renderer.d.ts`) so it can be subclassed from
     * Kotlin, matching the original's `MarkdownEditableComponentsRenderer`.
     */
    open class Renderer(options: MarkedOptions = definedExternally) {
        protected val options: MarkedOptions
        // Kotlin forbids overriding an external function that has default parameter values, so
        // these two (subclassed by MarkdownKomponentsRenderer) are declared with plain required
        // params — the real JS caller still supplies fewer args and lets the rest come through as
        // `undefined`, which these nullable Kotlin types accept without any external-side default.
        open fun code(code: String, lang: String?, escaped: Boolean?, meta: String?): String
        open fun blockquote(quote: String): String
        open fun html(html: String): String
        open fun heading(text: String, level: Int, raw: String): String
        open fun hr(): String
        open fun list(body: String, ordered: Boolean?): String
        open fun listitem(text: String): String
        open fun paragraph(text: String): String
        open fun table(header: String, body: String): String
        open fun tablerow(content: String): String
        open fun tablecell(content: String, flags: dynamic): String
        open fun strong(text: String): String
        open fun em(text: String): String
        open fun codespan(text: String): String
        open fun br(): String
        open fun del(text: String): String
        // the .d.ts declares `title: string` (non-optional), but the actual lexer passes JS
        // `undefined`/`null` when the markdown link/image has no title — nullable here to match
        // reality rather than the type declaration.
        open fun link(href: String, title: String?, text: String): String
        open fun image(href: String, title: String?, text: String): String
        open fun text(text: String): String
    }
}
