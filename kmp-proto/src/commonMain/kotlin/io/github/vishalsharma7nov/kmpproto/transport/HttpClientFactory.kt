package io.github.vishalsharma7nov.kmpproto.transport

import io.ktor.client.HttpClient

internal expect fun createDefaultHttpClient(): HttpClient
