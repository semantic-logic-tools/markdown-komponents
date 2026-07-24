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
            // optional `tsstack` package only — production code otherwise takes `parser` as a
            // plain (String) -> String the library consumer supplies. Unused unless a consumer
            // imports `tsstack.*`, in which case DCE should drop it from their bundle.
            implementation(npm("@ts-stack/markdown", "1.5.0"))
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
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
