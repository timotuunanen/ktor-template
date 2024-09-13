package com.tt.models

import io.ktor.http.HttpStatusCode

sealed class Failure(
    val message: String,
    val cause: String? = null,
    val code: HttpStatusCode
)

class DbFailure(exception: Throwable) : Failure(
    "Db call failed",
    exception.message,
    HttpStatusCode.InternalServerError
)

class TestClientFailure(exception: Throwable) : Failure(
    "Test client call failed",
    exception.message,
    HttpStatusCode.InternalServerError
)

class SomeFailure(msg: String) : Failure(
    message = msg,
    code = HttpStatusCode.BadRequest
)

class NotFoundFailure(msg: String) : Failure(
    message = msg,
    code = HttpStatusCode.NotFound
)
