package com.rm.scaleinkey

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.fetch.Response

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        // The browser has no system fonts to fall back on, and the built-in one lacks ♯ and ♭ (and
        // draws ▶ as a colour emoji), so a small font with them is preloaded as a fallback before
        // the UI first draws. If it can't be fetched the app still runs, with boxes for those.
        val fontFamilyResolver = LocalFontFamilyResolver.current
        var fontsReady by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            runCatching {
                fontFamilyResolver.preload(FontFamily(Font("ScaleInKeySymbols", fetchBytes("symbols.ttf"))))
            }
            document.getElementById("loading")?.remove()
            fontsReady = true
        }
        if (fontsReady) ScaleInKeyApp(WebPlatform)
    }
}

private suspend fun fetchBytes(url: String): ByteArray {
    val response = window.fetch(url).await<Response>()
    check(response.ok) { "$url: ${response.status}" }
    val bytes = Int8Array(response.arrayBuffer().await<ArrayBuffer>())
    return ByteArray(bytes.length) { bytes[it] }
}
