// App-level config template — copy into your app as kmp-proto.config.kt
// Keep this OUTSIDE the published library artifacts.

package com.example.app

import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
import io.github.vishalsharma7nov.kmpproto.ProtoSource
import io.github.vishalsharma7nov.kmpproto.TransportKind
import io.github.vishalsharma7nov.kmpproto.auth.createAuthInterceptor
import io.github.vishalsharma7nov.kmpproto.auth.AuthInterceptorOptions
import io.github.vishalsharma7nov.kmpproto.logging.createLoggingInterceptor
import io.github.vishalsharma7nov.kmpproto.path.PathTemplatePreset

val kmpProtoConfig = ProtoClientConfig(
    baseUrl = "https://api.example.com",
    protoSource = ProtoSource.local("protos"),
    // protoSource = ProtoSource.github(
    //     repo = "https://github.com/you/your-protos.git",
    //     ref = "v1.2.3",
    //     path = "protos",
    // ),
    // protoSource = ProtoSource.buf(
    //     module = "buf.build/acme/petapis",
    //     ref = "1.0.0",
    // ),
    getHeaders = {
        mapOf("Authorization" to "Bearer YOUR_TOKEN")
    },
    timeoutMs = 30_000,
    maxResponseBytes = 2 * 1024 * 1024,
    transport = TransportKind.Http,
    pathPreset = PathTemplatePreset.Connect,
    interceptors = listOf(
        createLoggingInterceptor(),
        // createAuthInterceptor(AuthInterceptorOptions(getToken = { "token" })),
    ),
)
