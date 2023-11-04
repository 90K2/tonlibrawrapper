package com.tonlibwrapper.tonlibranext.contracts

import com.tonlibwrapper.tonlibranext.LiteServerAccountId
import com.tonlibwrapper.tonlibranext.constants.ContractMethods
import com.tonlibwrapper.tonlibranext.getAccount
import com.tonlibwrapper.tonlibranext.toAddrString
import kotlinx.coroutines.delay
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.AccountActive
import org.ton.block.AddrStd
import org.ton.block.VmStack
import org.ton.block.VmStackValue
import org.ton.cell.Cell
import org.ton.lite.client.LiteClient

/**
 * The main purpose of this class is to give access
 * to different GET methods, using explicit contract address.
 * Instead of default address inside each contract implementation.
 */
open class LiteContract(
    override val liteClient: LiteClient
): RootContract(liteClient) {

    suspend fun isContractDeployed(address: String): Boolean {
        return liteClient.getAccount(address)?.storage?.state is AccountActive
    }

    suspend fun isContractDeployed(address: AddrStd): Boolean {
        return isContractDeployed(address.toAddrString())
    }

    private suspend fun runSmcRetry(
        address: AddrStd,
        method: String,
        lastBlockId: TonNodeBlockIdExt? = null,
        params: List<VmStackValue> = listOf()
    ): VmStack {
        return if (lastBlockId != null && params.isNotEmpty())
            liteClient.runSmcMethod(
                address = LiteServerAccountId(address),
                methodName = method,
                blockId = lastBlockId,
                params = params
            )
        else if (lastBlockId != null)
            liteClient.runSmcMethod(address = LiteServerAccountId(address), methodName = method, blockId = lastBlockId)
        else if (params.isNotEmpty())
            liteClient.runSmcMethod(address = LiteServerAccountId(address), methodName = method, params = params)
        else
            liteClient.runSmcMethod(address = LiteServerAccountId(address), methodName = method)
    }

    // I do not use AOP or spring-retry because of current class-architecture
    // we need to make retryable calls from other class that must be injected here
    // but this class is the Root of all contracts, so it will not be very handful
    // --
    // also for make aspects work we need to make everything open: logger, liteClient etc
    // probably liteClient must be removed from here somehow at all
    suspend fun runSmc(
        address: AddrStd,
        method: String,
        lastBlockId: TonNodeBlockIdExt? = null,
        params: List<VmStackValue> = listOf()
    ): VmStack? {
        var result: VmStack? = null
        var retryCount = 0
        var ex: Exception? = null
        while (result == null && retryCount < 4) {
            if (retryCount > 0) {
                delay(100)
//                println("Retry $retryCount $method for ${address.toAddrString()}")
            }
            try {
                result = runSmcRetry(address, method, lastBlockId, params)
            } catch (e: Exception) {
                ex = e
            }
            retryCount++
        }
        if (result == null && ex != null) {
            logger.warn("Error in $method for ${address.toAddrString()}")
            logger.warn(ex.message)
        }
//        }.onFailure {
//            logger.warn("Error in $method for ${address.toAddrString()}")
//            logger.warn(it.message)
//        }.getOrNull()

        return result
    }


    suspend fun getWalletPublicKey(walletAddress: AddrStd): BigInt {
        return runSmcRetry(walletAddress, ContractMethods.getWalletPublicKey).let {
            val stack = it.toMutableVmStack()
            stack.popInt()
        }
    }




    override fun createDataInit() = Cell.empty()

    override val sourceCode = Cell.empty()

}
