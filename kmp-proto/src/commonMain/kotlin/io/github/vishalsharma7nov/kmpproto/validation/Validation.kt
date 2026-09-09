package io.github.vishalsharma7nov.kmpproto.validation

import io.github.vishalsharma7nov.kmpproto.CodecRegistry
import io.github.vishalsharma7nov.kmpproto.MessageCodec
import io.github.vishalsharma7nov.kmpproto.errors.ProtoClientError
import io.github.vishalsharma7nov.kmpproto.errors.ProtoErrorCode

/** Optional runtime request validation before encode. */
public fun interface MessageValidator {
    public fun validate(message: Map<String, Any?>): String?
}

public fun withValidation(
    codec: MessageCodec,
    validator: MessageValidator,
): MessageCodec = object : MessageCodec {
    override fun encode(message: Map<String, Any?>): ByteArray {
        val error = validator.validate(message)
        if (error != null) {
            throw ProtoClientError(
                code = ProtoErrorCode.INVALID_ARGUMENT,
                message = error,
            )
        }
        return codec.encode(message)
    }

    override fun decode(bytes: ByteArray): Map<String, Any?> = codec.decode(bytes)
}

public fun wrapCodecRegistryWithValidation(
    registry: CodecRegistry,
    validators: Map<String, MessageValidator>,
): CodecRegistry = object : CodecRegistry {
    override fun get(typeName: String): MessageCodec? {
        val codec = registry.get(typeName) ?: return null
        val validator = validators[typeName] ?: return codec
        return withValidation(codec, validator)
    }

    override fun has(typeName: String): Boolean = registry.has(typeName)
}
