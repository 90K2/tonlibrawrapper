package com.tonlibwrapper.tonlibranext.common.exceptions

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    @JsonProperty(index = 1)
    val error: ErrorDetailResponse? = null
)

data class ErrorDetailResponse(
        val type: ErrorType,
        val message: String? = null,
        val error: Boolean = true
)

enum class ErrorType {
    SOMETHING_WENT_WRONG, ACCESS_DENIED, OBJECT_ALREADY_EXISTS, SERVICE_UNAVAILABLE, OBJECT_NOT_FOUND,
    WRONG_API_USAGE, TOO_MANY_REQUESTS, UNAUTHORIZED, BAD_REQUEST, TEMPORARY_UNAVAILABLE,
    ;
}
