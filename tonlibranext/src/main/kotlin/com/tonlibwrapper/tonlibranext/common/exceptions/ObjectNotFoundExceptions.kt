package com.tonlibwrapper.tonlibranext.common.exceptions

open class ObjectNotFoundException(cause: String? = null) : BaseException(type = ErrorType.OBJECT_NOT_FOUND) {
    override val message = cause ?: "Object not found"
}
