import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable() // the index.html dev harness: a self-running bundle for a <script> tag
        binaries.library()    // the actual published artifact: a real ES module for `import`
        compilerOptions {
            target = "es2015"
            moduleKind.set(JsModuleKind.MODULE_ES)
        }
    }

    sourceSets {
        jsMain.dependencies {
            implementation(npm("highlight.js", "10.5.0"))
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
            // reference implementation for parser tests only — production code takes `parser`
            // as a plain (String) -> String property the library consumer supplies
            implementation(npm("@ts-stack/markdown", "1.5.0"))
        }
    }
}

// The executable and library binaries sync into the same npm package
// directory, so the two production bundling tasks must each see both
// compile-sync outputs or Gradle flags an undeclared/unordered overlap.
tasks.named("jsBrowserProductionWebpack") {
    dependsOn("jsProductionLibraryCompileSync")
}
tasks.named("jsBrowserProductionLibraryDistribution") {
    dependsOn("jsProductionExecutableCompileSync")
}
