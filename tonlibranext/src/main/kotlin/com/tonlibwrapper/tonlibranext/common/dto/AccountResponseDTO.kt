package com.tonlibwrapper.tonlibranext.common.dto


data class AccountResponseDTO(
    val status: String,
    val balance: Long,
    val address: String,
    val addressRaw: String,
    val code: String? = null,
    val data: String? = null
)
