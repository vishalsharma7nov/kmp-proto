package io.github.vishalsharma7nov.kmpproto.codecs

import io.github.vishalsharma7nov.kmpproto.CodecRegistry
import io.github.vishalsharma7nov.kmpproto.MessageCodec
import okio.Buffer

/**
 * Minimal protobuf wire-format helpers for scalar fields used by generated codecs.
 * Field numbers follow the .proto definitions.
 */
public object ProtoWire {
    public const val WIRE_VARINT: Int = 0
    public const val WIRE_LENGTH_DELIMITED: Int = 2

    public fun writeTag(buffer: Buffer, fieldNumber: Int, wireType: Int) {
        writeVarint32(buffer, (fieldNumber shl 3) or wireType)
    }

    public fun writeVarint32(buffer: Buffer, value: Int) {
        var v = value
        while (true) {
            if (v and 0x7F.inv() == 0) {
                buffer.writeByte(v)
                return
            }
            buffer.writeByte((v and 0x7F) or 0x80)
            v = v ushr 7
        }
    }

    public fun writeVarint64(buffer: Buffer, value: Long) {
        var v = value
        while (true) {
            if (v and 0x7FL.inv() == 0L) {
                buffer.writeByte(v.toInt())
                return
            }
            buffer.writeByte((v.toInt() and 0x7F) or 0x80)
            v = v ushr 7
        }
    }

    public fun writeString(buffer: Buffer, fieldNumber: Int, value: String) {
        if (value.isEmpty()) return
        writeTag(buffer, fieldNumber, WIRE_LENGTH_DELIMITED)
        val bytes = value.encodeToByteArray()
        writeVarint32(buffer, bytes.size)
        buffer.write(bytes)
    }

    public fun writeInt32(buffer: Buffer, fieldNumber: Int, value: Int) {
        if (value == 0) return
        writeTag(buffer, fieldNumber, WIRE_VARINT)
        writeVarint32(buffer, value)
    }

    public fun writeInt64(buffer: Buffer, fieldNumber: Int, value: Long) {
        if (value == 0L) return
        writeTag(buffer, fieldNumber, WIRE_VARINT)
        writeVarint64(buffer, value)
    }

    public fun writeBool(buffer: Buffer, fieldNumber: Int, value: Boolean) {
        if (!value) return
        writeTag(buffer, fieldNumber, WIRE_VARINT)
        writeVarint32(buffer, 1)
    }

    public fun writeBytes(buffer: Buffer, fieldNumber: Int, value: ByteArray) {
        if (value.isEmpty()) return
        writeTag(buffer, fieldNumber, WIRE_LENGTH_DELIMITED)
        writeVarint32(buffer, value.size)
        buffer.write(value)
    }

    public fun writeMessage(buffer: Buffer, fieldNumber: Int, nested: ByteArray) {
        if (nested.isEmpty()) return
        writeTag(buffer, fieldNumber, WIRE_LENGTH_DELIMITED)
        writeVarint32(buffer, nested.size)
        buffer.write(nested)
    }

    public fun readVarint32(buffer: Buffer): Int {
        var result = 0
        var shift = 0
        while (shift < 32) {
            val b = buffer.readByte().toInt() and 0xFF
            result = result or ((b and 0x7F) shl shift)
            if (b and 0x80 == 0) return result
            shift += 7
        }
        error("Malformed varint32")
    }

    public fun readVarint64(buffer: Buffer): Long {
        var result = 0L
        var shift = 0
        while (shift < 64) {
            val b = buffer.readByte().toInt() and 0xFF
            result = result or ((b.toLong() and 0x7F) shl shift)
            if (b and 0x80 == 0) return result
            shift += 7
        }
        error("Malformed varint64")
    }

    public fun skip(buffer: Buffer, wireType: Int) {
        when (wireType) {
            WIRE_VARINT -> readVarint64(buffer)
            WIRE_LENGTH_DELIMITED -> {
                val len = readVarint32(buffer)
                buffer.skip(len.toLong())
            }
            1 -> buffer.skip(8) // 64-bit
            5 -> buffer.skip(4) // 32-bit
            else -> error("Unknown wire type $wireType")
        }
    }
}

public fun createCodecRegistry(codecs: Map<String, MessageCodec>): CodecRegistry =
    object : CodecRegistry {
        override fun get(typeName: String): MessageCodec? = codecs[typeName]
        override fun has(typeName: String): Boolean = codecs.containsKey(typeName)
    }

/**
 * Builds a codec from explicit field writers/readers (used by generated message codecs).
 */
public fun mapCodec(
    encode: (Map<String, Any?>, Buffer) -> Unit,
    decode: (Buffer) -> MutableMap<String, Any?>,
): MessageCodec = object : MessageCodec {
    override fun encode(message: Map<String, Any?>): ByteArray {
        val buffer = Buffer()
        encode(message, buffer)
        return buffer.readByteArray()
    }

    override fun decode(bytes: ByteArray): Map<String, Any?> {
        val buffer = Buffer().write(bytes)
        return decode(buffer)
    }
}

internal fun Map<String, Any?>.string(key: String): String =
    (this[key] as? String) ?: ""

internal fun Map<String, Any?>.int(key: String): Int =
    when (val v = this[key]) {
        is Int -> v
        is Long -> v.toInt()
        is Number -> v.toInt()
        else -> 0
    }

internal fun Map<String, Any?>.long(key: String): Long =
    when (val v = this[key]) {
        is Long -> v
        is Int -> v.toLong()
        is Number -> v.toLong()
        else -> 0L
    }

internal fun Map<String, Any?>.bool(key: String): Boolean =
    this[key] as? Boolean ?: false

@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any?>.listMaps(key: String): List<Map<String, Any?>> =
    (this[key] as? List<*>)?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()
