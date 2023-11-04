package com.tonlibwrapper.tonlibranext.client

import com.tonlibwrapper.tonlibranext.dto.TonTxRawDTO
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.Block
import org.ton.block.ShardDescrNew
import org.ton.lite.client.LiteClient

object BlockFun {

    fun getBlockId(workchain: Int, descr: ShardDescrNew) = TonNodeBlockIdExt(
        workchain = workchain,
        shard = descr.nextValidatorShard.toLong(),
        seqno = descr.seqNo.toInt(),
        rootHash = descr.rootHash.toByteArray(),
        fileHash = descr.fileHash.toByteArray()
    )


    suspend fun collectTransactions(block: Block, blockWc: Int, liteClient: LiteClient): MutableList<TonTxRawDTO> {
        val txs = mutableListOf<TonTxRawDTO>()

        block.extra.value.accountBlocks.value.iterator().forEach {
            it.second.value?.transactions?.iterator()?.forEach {
                it.second.value?.value?.let { tx ->
                    txs.add(TonTxRawDTO(tx, block.info.value.seqNo, blockWc))
                }
            }
        }
        block.extra.value.custom.value?.value?.shardHashes?.iterator()?.forEach {
            val workchain = BigInt(it.first.toByteArray()).toInt()
            it.second.nodes().toList().forEach {
                val shardBlock = getBlockId(workchain, it as ShardDescrNew)
                liteClient.getBlock(shardBlock)?.extra?.value?.accountBlocks?.value?.iterator()?.forEach {
                    it.second.value?.transactions?.iterator()?.forEach {
                        it.second.value?.value?.let { tx ->
                            txs.add(TonTxRawDTO(tx, shardBlock.seqno, workchain))
                        }
                    }
                }
            }
        }
        println("block $blockWc:${block.info.value.seqNo} found ${txs.size} txs inside")
        return txs
    }
}
