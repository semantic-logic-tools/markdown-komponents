package editor

import BaseWebComponent
import element
import org.w3c.dom.Element
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import style

/**
 * Inline SVGs instead of an icon web font (the original used Material Icons via `@font-face`).
 * A font is a network dependency that can silently fail to load, leaving ligature text like
 * "format_bold" visible instead of an icon; these are self-contained and always render.
 *
 * Uses plain SVG shapes/text rather than hand-traced icon paths — easier to get right without
 * being able to preview it, and `currentColor` picks up the button's text color either way.
 */
class ToolbarIcon : BaseWebComponent() {

    private val container: Element

    var icon: String
        get() = getAttribute("icon") ?: ""
        set(value) = setAttribute("icon", value)

    init {
        val shadow = attachShadow(ShadowRootInit(ShadowRootMode.OPEN))
        shadow.style(
            """
            :host {
              display: inline-flex;
              width: 24px;
              height: 24px;
              align-items: center;
              justify-content: center;
              color: inherit;
            }
            span, svg {
              display: block;
              width: 100%;
              height: 100%;
            }
            """.trimIndent()
        )
        container = shadow.element("span")
    }

    override fun attributeChangedCallback(name: String, oldValue: String?, newValue: String?) {
        if (name == "icon") {
            container.innerHTML = svgFor(newValue ?: "")
        }
    }

    private fun svgFor(icon: String): String = when (icon) {
        "bold" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <text x="12" y="17" text-anchor="middle" font-size="15" font-weight="900" font-family="serif">B</text>
            </svg>"""
        "italic" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <text x="12" y="17" text-anchor="middle" font-size="15" font-style="italic" font-family="serif">I</text>
            </svg>"""
        "strikethrough" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="currentColor">
                <text x="12" y="17" text-anchor="middle" font-size="15" font-family="serif" stroke="none">S</text>
                <line x1="4" y1="12" x2="20" y2="12" stroke-width="1.5"/>
            </svg>"""
        "list-bulleted" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <circle cx="4" cy="6" r="1.6"/><rect x="9" y="5" width="11" height="2"/>
                <circle cx="4" cy="12" r="1.6"/><rect x="9" y="11" width="11" height="2"/>
                <circle cx="4" cy="18" r="1.6"/><rect x="9" y="17" width="11" height="2"/>
            </svg>"""
        "list-numbered" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <text x="4" y="8" text-anchor="middle" font-size="7">1</text><rect x="9" y="5" width="11" height="2"/>
                <text x="4" y="14" text-anchor="middle" font-size="7">2</text><rect x="9" y="11" width="11" height="2"/>
                <text x="4" y="20" text-anchor="middle" font-size="7">3</text><rect x="9" y="17" width="11" height="2"/>
            </svg>"""
        "code-inline" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <text x="12" y="17" text-anchor="middle" font-size="15" font-family="monospace">{ }</text>
            </svg>"""
        "break" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <rect x="3" y="11" width="18" height="2.5"/>
            </svg>"""
        "insert-photo" ->
            """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="16" rx="1.5"/>
                <circle cx="8.5" cy="9.5" r="1.5" fill="currentColor" stroke="none"/>
                <polyline points="5,16 9,11 12,14 16,9 19,16" fill="none"/>
            </svg>"""
        "insert-link" ->
            """<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="1.5" y="9.5" width="9" height="5" rx="2.5" transform="rotate(-45 6 12)"/>
                <rect x="13.5" y="9.5" width="9" height="5" rx="2.5" transform="rotate(-45 18 12)"/>
            </svg>"""
        "code-block" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <text x="12" y="17" text-anchor="middle" font-size="12" font-family="monospace">&lt;/&gt;</text>
            </svg>"""
        "quote" ->
            """<svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <text x="12" y="19" text-anchor="middle" font-size="24" font-family="serif">"</text>
            </svg>"""
        else -> ""
    }
}
