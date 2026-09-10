package io.github.vishalsharma7nov.kmpproto.generated

import io.github.vishalsharma7nov.kmpproto.ProtoClient
import io.github.vishalsharma7nov.kmpproto.ProtoClientConfig
import io.github.vishalsharma7nov.kmpproto.client.createClientFromMap

/** Auto-generated typed API helpers. Do not edit by hand. */

public class UserServiceApi internal constructor(private val methods: Map<String, io.github.vishalsharma7nov.kmpproto.RpcMethod>) {
    public suspend fun addUser(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["addUser"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)
    public suspend fun getUser(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["getUser"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)
    public suspend fun getUserByEmail(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["getUserByEmail"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)
    public suspend fun listUsers(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["listUsers"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)
    public suspend fun updateUser(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["updateUser"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)
    public suspend fun deleteUser(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["deleteUser"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)
    public suspend fun getUserByPhone(request: Map<String, Any?> = emptyMap()): Map<String, Any?> = (methods["getUserByPhone"] as io.github.vishalsharma7nov.kmpproto.RpcMethod.Unary)(request)
}

public class GeneratedApi internal constructor(client: ProtoClient) {
    public val userService: UserServiceApi = UserServiceApi(client.services["userService"] ?: emptyMap())
}

public fun createApi(config: ProtoClientConfig): GeneratedApi =
    GeneratedApi(createClientFromMap(config, methodMap, defaultCodecs))
