external interface HighlightResult {
    val value: String
}

@JsModule("highlight.js")
external object hljs {
    fun highlight(languageName: String, code: String, ignoreIllegals: Boolean = definedExternally): HighlightResult
}
