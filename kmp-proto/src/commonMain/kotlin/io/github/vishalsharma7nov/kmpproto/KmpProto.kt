package io.github.vishalsharma7nov.kmpproto

import io.github.vishalsharma7nov.kmpproto.client.createClientFromMap
import io.github.vishalsharma7nov.kmpproto.codecs.createCodecRegistry
import io.github.vishalsharma7nov.kmpproto.generated.defaultCodecs
import io.github.vishalsharma7nov.kmpproto.generated.methodMap

public data class CreateClientOptions(
    val methodMap: MethodMap = io.github.vishalsharma7nov.kmpproto.generated.methodMap,
    val codecs: CodecRegistry = defaultCodecs,
)

/**
 * Creates a dynamic client using the package's generated method map and codecs.
 * Pass [CreateClientOptions] to override for tests or consumer-generated maps.
 */
public fun createClient(
    config: ProtoClientConfig,
    options: CreateClientOptions = CreateClientOptions(),
): ProtoClient = createClientFromMap(config, options.methodMap, options.codecs)

public fun createClient(
    config: ProtoClientConfig,
    methodMap: MethodMap,
    codecs: CodecRegistry,
): ProtoClient = createClientFromMap(config, methodMap, codecs)
