package com.tonlibwrapper.tonlibranext.common.exceptions

open class TemporaryUnavailableException(cause: String? = null) : BaseException() {
    override val message = cause ?: "Server is busy now try again later"
    override val type: ErrorType = ErrorType.TEMPORARY_UNAVAILABLE
}
