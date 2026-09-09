package io.github.vishalsharma7nov.kmpproto.transport

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

internal actual fun createDefaultHttpClient(): HttpClient = HttpClient(CIO)
