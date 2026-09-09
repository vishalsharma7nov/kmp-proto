package io.github.vishalsharma7nov.kmpproto.generated

import io.github.vishalsharma7nov.kmpproto.CodecRegistry
import io.github.vishalsharma7nov.kmpproto.MessageCodec
import io.github.vishalsharma7nov.kmpproto.codecs.ProtoWire
import io.github.vishalsharma7nov.kmpproto.codecs.createCodecRegistry
import io.github.vishalsharma7nov.kmpproto.codecs.mapCodec
import okio.Buffer

/** Auto-generated message codecs. Do not edit by hand. */

private fun encodeUser(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeString(buffer, 1, (message["id"] as? String) ?: "")
    ProtoWire.writeString(buffer, 2, (message["name"] as? String) ?: "")
    ProtoWire.writeString(buffer, 3, (message["email"] as? String) ?: "")
    ProtoWire.writeInt32(buffer, 4, ((message["age"] as? Number)?.toInt()) ?: 0)
    ProtoWire.writeString(buffer, 5, (message["phone"] as? String) ?: "")
    ProtoWire.writeInt64(buffer, 6, ((message["created_at"] as? Number)?.toLong()) ?: 0L)
    ProtoWire.writeInt64(buffer, 7, ((message["updated_at"] as? Number)?.toLong()) ?: 0L)
}
private fun decodeUser(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "id" to "",
        "name" to "",
        "email" to "",
        "age" to 0,
        "phone" to "",
        "created_at" to 0L,
        "updated_at" to 0L
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["id"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            2 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["name"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            3 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["email"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            4 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["age"] = ProtoWire.readVarint32(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            5 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["phone"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            6 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["created_at"] = ProtoWire.readVarint64(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            7 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["updated_at"] = ProtoWire.readVarint64(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeGetUserRequest(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeString(buffer, 1, (message["id"] as? String) ?: "")
}
private fun decodeGetUserRequest(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "id" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["id"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeGetUserByEmailRequest(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeString(buffer, 1, (message["email"] as? String) ?: "")
}
private fun decodeGetUserByEmailRequest(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "email" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["email"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeListUsersRequest(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeInt32(buffer, 1, ((message["page"] as? Number)?.toInt()) ?: 0)
    ProtoWire.writeInt32(buffer, 2, ((message["page_size"] as? Number)?.toInt()) ?: 0)
    ProtoWire.writeString(buffer, 3, (message["page_token"] as? String) ?: "")
    ProtoWire.writeString(buffer, 4, (message["query"] as? String) ?: "")
}
private fun decodeListUsersRequest(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "page" to 0,
        "page_size" to 0,
        "page_token" to "",
        "query" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["page"] = ProtoWire.readVarint32(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            2 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["page_size"] = ProtoWire.readVarint32(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            3 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["page_token"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            4 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["query"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeListUsersResponse(message: Map<String, Any?>, buffer: Buffer) {
    @Suppress("UNCHECKED_CAST")
    val usersList = (message["users"] as? List<*>)?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()
    for (item in usersList) {
        val nested = Buffer()
        encodeUser(item, nested)
        ProtoWire.writeMessage(buffer, 1, nested.readByteArray())
    }
    ProtoWire.writeString(buffer, 2, (message["next_page_token"] as? String) ?: "")
    ProtoWire.writeInt32(buffer, 3, ((message["total_count"] as? Number)?.toInt()) ?: 0)
}
private fun decodeListUsersResponse(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "users" to mutableListOf<Any?>(),
        "next_page_token" to "",
        "total_count" to 0
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    val nested = Buffer().write(buffer.readByteArray(len.toLong()))
                    @Suppress("UNCHECKED_CAST")
                    (out["users"] as MutableList<Any?>).add(decodeUser(nested))
                } else ProtoWire.skip(buffer, wire)
            }
            2 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["next_page_token"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            3 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["total_count"] = ProtoWire.readVarint32(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeAddUserRequest(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeString(buffer, 1, (message["name"] as? String) ?: "")
    ProtoWire.writeString(buffer, 2, (message["email"] as? String) ?: "")
    ProtoWire.writeInt32(buffer, 3, ((message["age"] as? Number)?.toInt()) ?: 0)
    ProtoWire.writeString(buffer, 4, (message["phone"] as? String) ?: "")
}
private fun decodeAddUserRequest(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "name" to "",
        "email" to "",
        "age" to 0,
        "phone" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["name"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            2 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["email"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            3 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["age"] = ProtoWire.readVarint32(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            4 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["phone"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeUpdateUserRequest(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeString(buffer, 1, (message["id"] as? String) ?: "")
    ProtoWire.writeString(buffer, 2, (message["name"] as? String) ?: "")
    ProtoWire.writeString(buffer, 3, (message["email"] as? String) ?: "")
    ProtoWire.writeInt32(buffer, 4, ((message["age"] as? Number)?.toInt()) ?: 0)
    ProtoWire.writeString(buffer, 5, (message["phone"] as? String) ?: "")
}
private fun decodeUpdateUserRequest(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "id" to "",
        "name" to "",
        "email" to "",
        "age" to 0,
        "phone" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["id"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            2 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["name"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            3 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["email"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            4 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["age"] = ProtoWire.readVarint32(buffer)
                else ProtoWire.skip(buffer, wire)
            }
            5 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["phone"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeDeleteUserRequest(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeString(buffer, 1, (message["id"] as? String) ?: "")
}
private fun decodeDeleteUserRequest(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "id" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["id"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeDeleteUserResponse(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeBool(buffer, 1, message["success"] as? Boolean ?: false)
    ProtoWire.writeString(buffer, 2, (message["id"] as? String) ?: "")
}
private fun decodeDeleteUserResponse(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "success" to false,
        "id" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_VARINT) out["success"] = ProtoWire.readVarint32(buffer) != 0
                else ProtoWire.skip(buffer, wire)
            }
            2 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["id"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}

private fun encodeGetUserByPhoneRequest(message: Map<String, Any?>, buffer: Buffer) {
    ProtoWire.writeString(buffer, 1, (message["phone"] as? String) ?: "")
}
private fun decodeGetUserByPhoneRequest(buffer: Buffer): MutableMap<String, Any?> {
    val out = mutableMapOf<String, Any?>(
        "phone" to ""
    )
    while (!buffer.exhausted()) {
        val tag = ProtoWire.readVarint32(buffer)
        val field = tag ushr 3
        val wire = tag and 0x7
        when (field) {
            1 -> {
                if (wire == ProtoWire.WIRE_LENGTH_DELIMITED) {
                    val len = ProtoWire.readVarint32(buffer)
                    out["phone"] = buffer.readByteArray(len.toLong()).decodeToString()
                } else ProtoWire.skip(buffer, wire)
            }
            else -> ProtoWire.skip(buffer, wire)
        }
    }
    return out
}


public val User: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeUser(message, buffer) },
    decode = { buffer -> decodeUser(buffer) },
)

public val GetUserRequest: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeGetUserRequest(message, buffer) },
    decode = { buffer -> decodeGetUserRequest(buffer) },
)

public val GetUserByEmailRequest: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeGetUserByEmailRequest(message, buffer) },
    decode = { buffer -> decodeGetUserByEmailRequest(buffer) },
)

public val ListUsersRequest: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeListUsersRequest(message, buffer) },
    decode = { buffer -> decodeListUsersRequest(buffer) },
)

public val ListUsersResponse: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeListUsersResponse(message, buffer) },
    decode = { buffer -> decodeListUsersResponse(buffer) },
)

public val AddUserRequest: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeAddUserRequest(message, buffer) },
    decode = { buffer -> decodeAddUserRequest(buffer) },
)

public val UpdateUserRequest: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeUpdateUserRequest(message, buffer) },
    decode = { buffer -> decodeUpdateUserRequest(buffer) },
)

public val DeleteUserRequest: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeDeleteUserRequest(message, buffer) },
    decode = { buffer -> decodeDeleteUserRequest(buffer) },
)

public val DeleteUserResponse: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeDeleteUserResponse(message, buffer) },
    decode = { buffer -> decodeDeleteUserResponse(buffer) },
)

public val GetUserByPhoneRequest: MessageCodec = mapCodec(
    encode = { message, buffer -> encodeGetUserByPhoneRequest(message, buffer) },
    decode = { buffer -> decodeGetUserByPhoneRequest(buffer) },
)


public val defaultCodecs: CodecRegistry = createCodecRegistry(
    mapOf(
        "demo.User" to User,
        "demo.GetUserRequest" to GetUserRequest,
        "demo.GetUserByEmailRequest" to GetUserByEmailRequest,
        "demo.ListUsersRequest" to ListUsersRequest,
        "demo.ListUsersResponse" to ListUsersResponse,
        "demo.AddUserRequest" to AddUserRequest,
        "demo.UpdateUserRequest" to UpdateUserRequest,
        "demo.DeleteUserRequest" to DeleteUserRequest,
        "demo.DeleteUserResponse" to DeleteUserResponse,
        "demo.GetUserByPhoneRequest" to GetUserByPhoneRequest
    ),
)
