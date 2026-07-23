package editor

import BaseWebComponent
import org.w3c.dom.OPEN
import org.w3c.dom.ShadowRootInit
import org.w3c.dom.ShadowRootMode
import slot
import style

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
