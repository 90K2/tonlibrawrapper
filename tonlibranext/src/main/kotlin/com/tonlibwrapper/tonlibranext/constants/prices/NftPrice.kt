package com.tonlibwrapper.tonlibranext.constants.prices

import com.tonlibwrapper.tonlibranext.common.Extensions.toNano


object NftPrice {
    val DEPLOYMENT_PRICE = 0.073.toNano() //includes 0.05 for MIN_BALANCE
    val TRANSFER_PRICE = 0.02.toNano() // it can be more, if NFT balance < 0.05 TON.
    val MINTING_PRICE = 0.072.toNano()

    val TON_OPERATION = 0.017.toNano()

    val NFT_COLLECTION_DEPLOYMENT_PRICE = 0.011.toNano()
    val COLLECTION_TRANSFER_PRICE = 0.012.toNano()
}
