package com.tonlibwrapper.tonlibranext.common.exceptions

open class BaseException(open val type: ErrorType = ErrorType.SOMETHING_WENT_WRONG) : RuntimeException() {
    override val message = "Unprocessable request"
}

open class SomethingWentWrong(cause: String? = null) : BaseException() {
    override val message = cause ?: "Something Went Wrong"
    override val type = ErrorType.SOMETHING_WENT_WRONG
}

class ServiceUnavailableException(cause: String? = null) : BaseException() {
    override val message = cause ?: "Service currently on maintenance"
    override val type = ErrorType.SERVICE_UNAVAILABLE
}
