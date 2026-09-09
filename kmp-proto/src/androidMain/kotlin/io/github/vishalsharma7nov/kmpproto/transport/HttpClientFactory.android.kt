package io.github.vishalsharma7nov.kmpproto.transport

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createDefaultHttpClient(): HttpClient = HttpClient(OkHttp)
