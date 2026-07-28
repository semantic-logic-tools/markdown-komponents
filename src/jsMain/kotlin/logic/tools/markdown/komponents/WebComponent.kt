package logic.tools.markdown.komponents

import org.w3c.dom.*
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.js.JsClass

/**
 * Kotlin's [org.w3c.dom.HTMLElement] binding leaves several members abstract — the
 * ParentNode/ChildNode/GeometryUtils/ElementCSSInlineStyle mixin members and the style property —
 * because the bindings never implement them themselves; the real browser prototype does. A plain
 * `class Foo : HTMLElement()` would have to "implement" all of them just to satisfy the compiler.
 *
 * `@JsName("HTMLElement")` makes this a second Kotlin type for the very same global HTMLElement
 * constructor. Redeclaring the leftover members here without bodies closes out that abstractness
 * (an external member without a body means "the JS runtime already provides this"), so real
 * subclasses of [WebComponent] only need to implement what they actually use.
 *
 * It also carries the custom element lifecycle callbacks. The browser invokes these by their exact
 * name (`instance.connectedCallback()`), so they can't be plain Kotlin methods — those get name
 * -mangled. Declaring them here and overriding them in [BaseWebComponent] keeps the literal name.
 */
@JsName("HTMLElement")
abstract external class WebComponent : HTMLElement {
    abstract fun connectedCallback()
    abstract fun disconnectedCallback()
    abstract fun adoptedCallback()
    abstract fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?)

    // Left abstract by the HTMLElement binding; see the class doc above.
    override val style: CSSStyleDeclaration
    override var contentEditable: String
    override val isContentEditable: Boolean
    override val children: HTMLCollection
    override val childElementCount: Int
    override fun querySelector(selectors: String): Element?
    override fun querySelectorAll(selectors: String): NodeList
    override fun append(vararg nodes: dynamic)
    override fun prepend(vararg nodes: dynamic)
    override fun before(vararg nodes: dynamic)
    override fun after(vararg nodes: dynamic)
    override fun replaceWith(vararg nodes: dynamic)
    override fun remove()
    override fun getBoxQuads(options: BoxQuadOptions): Array<DOMQuad>
    override fun convertQuadFromNode(quad: dynamic, from: dynamic, options: ConvertCoordinateOptions): DOMQuad
    override fun convertRectFromNode(rect: DOMRectReadOnly, from: dynamic, options: ConvertCoordinateOptions): DOMQuad
    override fun convertPointFromNode(point: DOMPointInit, from: dynamic, options: ConvertCoordinateOptions): DOMPoint
}

@OptIn(ExperimentalJsExport::class)
@JsExport
abstract class BaseWebComponent : WebComponent() {
    override fun connectedCallback() {}
    override fun disconnectedCallback() {}
    override fun adoptedCallback() {}
    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {}
}

/**
 * [CustomElementRegistry.define] is typed as `() -> dynamic` in kotlin-dom-api-compat because it
 * predates ES2015 class generation. Kotlin/JS classes compiled with `useEsClasses` are real JS
 * classes, so `MyElement::class.js` is what the browser actually expects here; this overload lets
 * call sites register a component without reaching for `asDynamic()` themselves.
 *
 * [observedAttributes] becomes the class's static `observedAttributes` getter, which the browser
 * reads once during `define()` to decide which attribute changes actually invoke
 * `attributeChangedCallback`. There's no typed way to declare a static member on an external class
 * alias like [WebComponent], so this is the one place that reaches for `asDynamic()`.
 */
fun CustomElementRegistry.define(
    name: String,
    elementClass: JsClass<out WebComponent>,
    observedAttributes: Array<String> = emptyArray(),
) {
    if (observedAttributes.isNotEmpty()) {
        elementClass.asDynamic().observedAttributes = observedAttributes
    }
    define(name, elementClass.unsafeCast<() -> dynamic>())
}
