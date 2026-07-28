// Hand-written TypeScript declarations for this package's public API.
//
// Kotlin/JS's own `generateTypeScriptDefinitions()` was tried first, but its generated `extends`
// clauses are broken for any exported class whose supertype chain touches an *external* class
// (this library's entire custom-element hierarchy is rooted in `HTMLElement`, and
// `TsStackMarkdownRenderer` is rooted in `@ts-stack/markdown`'s `Renderer`) — the emitted reference
// to that supertype's `$metadata$` companion doesn't resolve, and members overriding the external
// base are silently dropped from the declaration. Patching that generator output turned out to be
// more fragile than just declaring the (small, stable) consumer-facing surface directly.
//
// Plain `.d.ts`, not `.d.mts`: the `.d.mts` extension didn't exist before TypeScript 4.7 (2022), so
// older TypeScript (e.g. 3.9, confirmed against a real consumer) can't find it via package.json's
// "types" field at all. This file has no ESM-specific syntax in it, so a plain `.d.ts` describes the
// same shape and works everywhere.
//
// Keep this in sync with the real Kotlin source under src/jsMain/kotlin — it is NOT derived from it
// automatically. `@JsExport` in the Kotlin source is what makes these actually exist as runtime
// bindings; this file only describes their shape to TypeScript.

export interface MarkdownElement {
  getMarkdown(): string;

  /** True for elements that cannot live inside other blocks, but must be a direct child of markdown-document. */
  readonly mustBeDirectChildOfDocument: boolean;
}

export interface MarkdownRenderer {
  heading(text: string, level: number, id: string | null | undefined): string;
  paragraph(text: string): string;
  blockquote(quote: string): string;
  html(html: string): string;
  hr(): string;
  list(body: string, ordered: boolean): string;
  listItem(text: string): string;
  taskListItem(text: string, checked: boolean): string;
  table(header: string, body: string): string;
  tableRow(content: string): string;
  tableHeaderRow(content: string): string;
  tableCell(content: string): string;
  tableHeaderCell(content: string): string;
  code(code: string, lang: string | null | undefined, id: string | null | undefined): string;
  codeSpan(text: string): string;
  strong(text: string): string;
  emphasis(text: string): string;
  strikethrough(text: string): string;
  link(href: string, title: string | null | undefined, text: string): string;
  image(href: string, title: string | null | undefined, text: string): string;
  toc(): string;
  custom(tag: string, content: string): string;
}

/** The library's own default `MarkdownRenderer`: renders straight to this library's `<markdown-*>` custom elements. */
export class MarkdownComponentsRenderer implements MarkdownRenderer {
  constructor();
  heading(text: string, level: number, id: string | null | undefined): string;
  paragraph(text: string): string;
  blockquote(quote: string): string;
  html(html: string): string;
  hr(): string;
  list(body: string, ordered: boolean): string;
  listItem(text: string): string;
  taskListItem(text: string, checked: boolean): string;
  table(header: string, body: string): string;
  tableRow(content: string): string;
  tableHeaderRow(content: string): string;
  tableCell(content: string): string;
  tableHeaderCell(content: string): string;
  code(code: string, lang: string | null | undefined, id: string | null | undefined): string;
  codeSpan(text: string): string;
  strong(text: string): string;
  emphasis(text: string): string;
  strikethrough(text: string): string;
  link(href: string, title: string | null | undefined, text: string): string;
  image(href: string, title: string | null | undefined, text: string): string;
  toc(): string;
  custom(tag: string, content: string): string;
}

/**
 * Adapts `@ts-stack/markdown`'s token-driven `Renderer` callback API to this library's
 * `MarkdownRenderer` contract. Subclass this (not `MarkdownRenderer` directly) when you need both
 * an overridable per-construct method (e.g. `image()`) *and* ts-stack's `options` hook (e.g.
 * `options.escape`) from one place — pass an instance to `parseMarkdownWithTsStackRenderer`.
 */
export class TsStackMarkdownRenderer {
  constructor(delegate?: MarkdownRenderer);
  // `encode` optional to match the real `@ts-stack/markdown` type
  // (node_modules/@ts-stack/markdown/src/interfaces.d.ts) it's meant to reflect.
  protected readonly options: { escape?: (html: string, encode?: boolean) => string };
  /** Also called directly by this library's fenced-code-with-explicit-id block rule. */
  codeWithAnchor(code: string, lang: string | null | undefined, id: string | null | undefined, escaped?: boolean | null): string;
  code(code: string, lang: string | null | undefined, escaped: boolean | null | undefined, meta: string | null | undefined): string;
  blockquote(quote: string): string;
  html(html: string): string;
  heading(text: string, level: number, raw: string): string;
  hr(): string;
  list(body: string, ordered: boolean | null | undefined): string;
  listitem(text: string): string;
  paragraph(text: string): string;
  table(header: string, body: string): string;
  tablerow(content: string): string;
  tablecell(content: string, flags: any): string;
  strong(text: string): string;
  em(text: string): string;
  codespan(text: string): string;
  del(text: string): string;
  link(href: string, title: string | null | undefined, text: string): string;
  image(href: string, title: string | null | undefined, text: string): string;
}

/**
 * Parses `markdown` using `@ts-stack/markdown`, driving `renderer` (this library's own components
 * by default) to produce the resulting `<markdown-*>` HTML. Assignable directly to
 * `markdownDocument.parser`, e.g. `doc.parser = parseMarkdown`. For a renderer that also needs
 * ts-stack's `options` hook, use `parseMarkdownWithTsStackRenderer` instead.
 */
export function parseMarkdown(markdown: string, renderer?: MarkdownRenderer): string;

/** Like `parseMarkdown`, but for a `renderer` that needs ts-stack's `options` hook alongside overriding per-construct methods. */
export function parseMarkdownWithTsStackRenderer(markdown: string, renderer: TsStackMarkdownRenderer): string;

export abstract class BaseMarkdownDocument extends HTMLElement {
  /**
   * Converts markdown text to the HTML string of `<markdown-*>` tags this element sets as
   * `innerHTML`. Defaults to a passthrough that just wraps the text in a paragraph; plug in
   * `parseMarkdown`/`parseMarkdownWithTsStackRenderer`, or any `(markdown: string) => string`.
   */
  parser: (markdown: string) => string;
  markdown: string;
  toolbar: Element | null;
  // Document | ShadowRoot: a consumer setting this from inside a shadow-DOM component (the intended
  // use — see getSelection() below) has a real ShadowRoot here, not a Document.
  selectionRoot: Document | ShadowRoot;
  onLinkClick: ((url: string) => void) | null;
  autoNormalize: boolean;
  readonly currentSelection: Selection | null;
  readonly editable: boolean;

  getSelection(): Selection | null;
  contentLengthUntil(child: Node | null): number;
  normalizeContent(): void;
  contentLength(): number;
  setToolbar(newToolbar: Element): void;
  getMarkdown(): string;
  getCurrentLeafBlock(): Element | null;
  getLastLeafBlock(): Element | null;
  onChange(): void;

  makeBreak(): void;
  makeBold(): void;
  removeBold(): void;
  wrapCurrentSelectionInNewElement(elementName: string): Element | null;
  makeItalic(): void;
  removeItalic(): void;
  makeUnderline(): void;
  makeStrike(): void;
  removeStrike(): void;
  makeCodeInline(): void;
  listBulletedClick(): void;
  listNumericClick(): void;
  insertPhoto(url: string | null, text: string | null): void;
  restoreStashedSelection(): void;
  insertLink(): void;
  header1Element(): void;
  header2Element(): void;
  header3Element(): void;
  header4Element(): void;
  header5Element(): void;
  header6Element(): void;
  pararaphElement(): void;
  /** Reassignable wholesale (e.g. `doc.makeCodeBlock = () => {...}`) to fully replace the default behavior. */
  makeCodeBlock(): void;
  makeQuoteBlock(): void;
}

/** `<markdown-document>` */
export class MarkdownDocument extends BaseMarkdownDocument {}

/** `<markdown-editor>` — like `MarkdownDocument`, but with its own built-in toolbar. */
export class MarkdownEditor extends BaseMarkdownDocument {}

/** `<markdown-toolbar>` — opaque from the outside: only ever cast to and passed into `setToolbar()`. */
export class MarkdownToolbar extends HTMLElement {}

/** `<markdown-toc>` */
export class MarkdownToc extends HTMLElement {
  markdownDocument: Element | null;
  /** Rebuilds the table of contents from `markdownDocument`. Runs automatically when that property is set. */
  refresh(): void;
  getMarkdown(): string;
}

/** `<markdown-image>`. Open for subclassing to customize how an image renders, e.g. inlining a data URI differently. */
export class MarkdownImage extends HTMLElement implements MarkdownElement {
  destination: string;
  readonly mustBeDirectChildOfDocument: boolean;
  /** Updates the shadow DOM to reflect current state. Override to customize how the image is displayed. */
  protected render(): void;
  setImageSrc(src: string): void;
  getMarkdown(): string;
  containsMarkdownTextContent(): boolean;
  isDeletableAsAWhole(): boolean;
}

/** `<markdown-paragraph>` */
export class MarkdownParagraph extends HTMLElement {
  getMarkdown(): string;
  isEmpty(): boolean;
}

declare global {
  interface HTMLElementTagNameMap {
    'markdown-document': MarkdownDocument;
    'markdown-editor': MarkdownEditor;
    'markdown-toolbar': MarkdownToolbar;
    'markdown-toc': MarkdownToc;
    'markdown-image': MarkdownImage;
    'markdown-paragraph': MarkdownParagraph;
  }
}
