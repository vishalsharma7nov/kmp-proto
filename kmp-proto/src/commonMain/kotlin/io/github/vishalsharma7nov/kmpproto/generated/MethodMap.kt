package io.github.vishalsharma7nov.kmpproto.generated

import io.github.vishalsharma7nov.kmpproto.MethodKind
import io.github.vishalsharma7nov.kmpproto.MethodMap
import io.github.vishalsharma7nov.kmpproto.MethodMapEntry

/** Auto-generated method map. Do not edit by hand. */
public val methodMap: MethodMap = listOf(
    MethodMapEntry(
        serviceName = "UserService",
        methodName = "AddUser",
        packageName = "demo",
        rpcPath = "demo.UserService/AddUser",
        requestType = "demo.AddUserRequest",
        responseType = "demo.User",
        kind = MethodKind.Unary,
    ),
    MethodMapEntry(
        serviceName = "UserService",
        methodName = "GetUser",
        packageName = "demo",
        rpcPath = "demo.UserService/GetUser",
        requestType = "demo.GetUserRequest",
        responseType = "demo.User",
        kind = MethodKind.Unary,
    ),
    MethodMapEntry(
        serviceName = "UserService",
        methodName = "GetUserByEmail",
        packageName = "demo",
        rpcPath = "demo.UserService/GetUserByEmail",
        requestType = "demo.GetUserByEmailRequest",
        responseType = "demo.User",
        kind = MethodKind.Unary,
    ),
    MethodMapEntry(
        serviceName = "UserService",
        methodName = "ListUsers",
        packageName = "demo",
        rpcPath = "demo.UserService/ListUsers",
        requestType = "demo.ListUsersRequest",
        responseType = "demo.ListUsersResponse",
        kind = MethodKind.Unary,
    ),
    MethodMapEntry(
        serviceName = "UserService",
        methodName = "UpdateUser",
        packageName = "demo",
        rpcPath = "demo.UserService/UpdateUser",
        requestType = "demo.UpdateUserRequest",
        responseType = "demo.User",
        kind = MethodKind.Unary,
    ),
    MethodMapEntry(
        serviceName = "UserService",
        methodName = "DeleteUser",
        packageName = "demo",
        rpcPath = "demo.UserService/DeleteUser",
        requestType = "demo.DeleteUserRequest",
        responseType = "demo.DeleteUserResponse",
        kind = MethodKind.Unary,
    ),
    MethodMapEntry(
        serviceName = "UserService",
        methodName = "GetUserByPhone",
        packageName = "demo",
        rpcPath = "demo.UserService/GetUserByPhone",
        requestType = "demo.GetUserByPhoneRequest",
        responseType = "demo.User",
        kind = MethodKind.Unary,
    ),

)
