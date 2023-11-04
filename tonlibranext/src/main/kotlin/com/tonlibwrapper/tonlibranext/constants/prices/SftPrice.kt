package com.tonlibwrapper.tonlibranext.constants.prices

import com.tonlibwrapper.common.Extensions.toNano

object SftPrice {

    val SFT_OPERATION = 0.01.toNano()

    val SFT_COLLECTION_DEPLOY = 0.0005.toNano()

    // mint sft collection item
    val SFT_MINT_MINTER_OPERATION = 0.027.toNano()
    val SFT_MINTER_DEPLOY = 0.01.toNano() + 0.1.toNano()
    // mint sft tokens
    val SFT_MINTER_MINT = 0.01.toNano() + 0.02.toNano()
    // single
    val SFT_DEPLOY = 0.01.toNano() + 0.02.toNano()

    val SFT_SELLER_DEPLOY = 0.5.toNano()
    val SFT_TRANSFER = 0.05.toNano()

    val SELLER_GAS = 1.0.toNano()
}
