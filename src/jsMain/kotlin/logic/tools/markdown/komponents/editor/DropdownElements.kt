package logic.tools.markdown.komponents.editor

import logic.tools.markdown.komponents.BaseWebComponent
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import logic.tools.markdown.komponents.slot
import logic.tools.markdown.komponents.style

class DropdownElements : BaseWebComponent() {
    init {
        attachShadow(ShadowRootInit(ShadowRootMode.OPEN)).apply {
            style(
                """
                :host {
                  display: flex;
                  flex-direction: column;
                  border: 1px solid gray;
                  background-color: white;
                  box-shadow: 0px 0px 4px gray;
                  padding: 10px;
                }
                """.trimIndent()
            )
            slot()
        }
    }
}
