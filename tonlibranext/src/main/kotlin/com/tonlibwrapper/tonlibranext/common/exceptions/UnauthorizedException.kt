package com.tonlibwrapper.tonlibranext.common.exceptions

class UnauthorizedException(message: String? = null) : BaseException(type = ErrorType.UNAUTHORIZED) {
    override val message = message ?: "Unauthorized"
}
