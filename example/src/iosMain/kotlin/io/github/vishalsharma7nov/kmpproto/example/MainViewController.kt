package io.github.vishalsharma7nov.kmpproto.example

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point for the Compose Multiplatform example.
 * The Xcode app (`iosApp`) hosts this view controller.
 */
fun MainViewController(): UIViewController =
    ComposeUIViewController { App() }
