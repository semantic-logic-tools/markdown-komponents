import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "logic.tools"
version = "0.1.0"

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
        compilations["main"].packageJson {
            customField("description", "Markdown editor web components (Kotlin/JS)")
            customField("license", "EUPL-1.2")
            customField("repository", mapOf("type" to "git", "url" to "git+https://github.com/semantic-logic-tools/markdown-komponents.git"))
            customField("keywords", listOf("markdown", "web-components", "kotlin-js", "wysiwyg"))
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

// Publishes build/dist/js/productionLibrary (the library artifact, not the dev-harness
// executable). Defaults to `npm publish --dry-run` — pass -PnpmDryRun=false for a real publish.
tasks.register<Exec>("publishNpm") {
    dependsOn("jsBrowserProductionLibraryDistribution")
    workingDir = layout.buildDirectory.dir("dist/js/productionLibrary").get().asFile

    val dryRun = (findProperty("npmDryRun") as String?)?.toBooleanStrictOrNull() ?: true
    val npmCommand = if (org.gradle.internal.os.OperatingSystem.current().isWindows) "npm.cmd" else "npm"
    commandLine = listOfNotNull(npmCommand, "publish", "--dry-run".takeIf { dryRun })

    doFirst {
        if (dryRun) logger.lifecycle("Dry run only (pass -PnpmDryRun=false to actually publish)")
    }
}
