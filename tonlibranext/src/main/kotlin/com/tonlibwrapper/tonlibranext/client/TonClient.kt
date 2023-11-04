package com.tonlibwrapper.tonlibranext.client

import com.tonlibwrapper.tonlibranext.common.exceptions.ObjectNotFoundException
import com.tonlibwrapper.tonlibranext.dto.TonTxRawDTO
import org.springframework.stereotype.Component
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.block.AccountInfo
import org.ton.block.AccountNone
import org.ton.block.AddrStd
import org.ton.block.Block
import org.ton.lite.client.LiteClient


@Component
class TonClient(
    private val liteClient: LiteClient
) {

    /**
     * @address :base64 user friendly address string
     */
    suspend fun getAccount(address: String): AccountInfo? = getAccount(AddrStd(address))

    suspend fun getAccount(address: AddrStd): AccountInfo? {
        val account = liteClient.getAccountState(address).account.value
        return when (account) {
            is AccountInfo -> account as AccountInfo
            is AccountNone -> null
            else -> null
        }
    }

    /** used only in tests
     * wc = -1 ;; master bock
     * wc = 0 ;; shard block
     */
    suspend fun loadBlockTransactions(liteClient: LiteClient, wc: Int, seqNo: Int): MutableList<TonTxRawDTO> {
        val block = liteClient.lookupBlock(TonNodeBlockId(
            workchain = wc,
            shard = Long.MIN_VALUE,
            seqno = seqNo
        )) ?: throw ObjectNotFoundException("Block not found")

        return BlockFun.collectTransactions(liteClient.getBlock(block)!!, wc, liteClient)
    }

    suspend fun loadBlockTransactions(liteClient: LiteClient, block: Block?, wc: Int): MutableList<TonTxRawDTO> {
        if (block == null) return mutableListOf()
        return BlockFun.collectTransactions(block, wc, liteClient)
    }
}

