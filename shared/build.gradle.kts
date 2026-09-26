import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

// Everything but the audio engine and the Android entry point lives here, shared by the app and
// the browser build (:shared:wasmJsBrowserDistribution).
kotlin {
    android {
        namespace = "com.rm.scaleinkey.shared"
        compileSdk = 37
        minSdk = 26
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "scaleinkey"
        browser {
            commonWebpackConfig {
                outputFileName = "scaleinkey.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jb.compose.runtime)
            implementation(libs.jb.compose.foundation)
            implementation(libs.jb.compose.ui)
            implementation(libs.jb.compose.material3)
            implementation(libs.kotlinx.coroutines.core)
        }
        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
            // The bundled soundfont and its notice, served beside the page.
            resources.srcDir(rootProject.file("app/src/main/assets"))
        }
    }
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenEnvSpec>().downloadBaseUrl.set(null as String?)
}

/**
 * What the page downloads before the app can start, and how big each is, for
 * its loading bar (index.html): load-sizes.json beside it. Written into the
 * distribution when it is made, since the WebAssembly files' names are the
 * bundler's hashes, and so goes wherever the distribution is published.
 */
listOf(
    "wasmJsBrowserDistribution" to "productionExecutable",
    "wasmJsBrowserDevelopmentExecutableDistribution" to "developmentExecutable",
).forEach { (task, dir) ->
    val dist = layout.buildDirectory.dir("dist/wasmJs/$dir").get().asFile
    tasks.matching { it.name == task }.configureEach {
        doLast {
            val files = dist.listFiles().orEmpty()
                .filter { it.isFile && (it.name.endsWith(".wasm") || it.name.endsWith(".js")) }
                .sortedBy { it.name }
            dist.resolve("load-sizes.json").writeText(
                files.joinToString(",\n", "{\n", "\n}\n") { "  \"${it.name}\": ${it.length()}" },
            )
        }
    }
}
