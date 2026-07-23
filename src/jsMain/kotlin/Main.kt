/**
 * `binaries.executable()` requires an entry point to exist even though this library does its
 * actual setup via AutoRegister.kt's eager module-load side effect (see there for why) — the dev
 * harness (index.html) still needs a script to point at, so this stays, empty.
 */
fun main() {
}
