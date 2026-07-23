package markdown.components

import markdown.Heading

class MarkdownHeading1 : Heading() {
    override val depth = 1
    init { renderHeading() }
}

class MarkdownHeading2 : Heading() {
    override val depth = 2
    init { renderHeading() }
}

class MarkdownHeading3 : Heading() {
    override val depth = 3
    init { renderHeading() }
}

class MarkdownHeading4 : Heading() {
    override val depth = 4
    init { renderHeading() }
}

class MarkdownHeading5 : Heading() {
    override val depth = 5
    init { renderHeading() }
}

class MarkdownHeading6 : Heading() {
    override val depth = 6
    init { renderHeading() }
}
