package com.tonlibwrapper.tonlibranext

import com.tonlibwrapper.tonlibranext.common.Extensions.toBinaryArray
import com.tonlibwrapper.tonlibranext.constants.SendMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.slf4j.Logger
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.contract.ContentData
import org.ton.contract.SnakeData
import org.ton.contract.SnakeDataCons
import org.ton.contract.SnakeDataTail
import org.ton.contract.wallet.WalletTransfer
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import org.ton.tlb.storeTlb


fun CellSlice.loadRemainingBits(): BitString {
    return BitString((this.bitsPosition until this.bits.size).map { this.loadBit() })
}

fun CellSlice.loadRemainingBitsAll(): BitString {
    var r = BitString((this.bitsPosition until this.bits.size).map { this.loadBit() })
    if (this.refs.isNotEmpty()) {
        r += this.refs.first().beginParse().loadRemainingBitsAll()
    }

    return r
}

fun MsgAddress.toAddrString() = (this as AddrStd).toString(true)

fun AccountState?.getState(): StateInit? {
    return when (this) {
        is AccountActive -> this.value
        else -> null
    }
}

fun BitString.clone() = BitString.of(this.toByteArray(), this.size)

fun AddrStd.toSlice() = CellBuilder.createCell {
    storeTlb(MsgAddress, this@toSlice)
}.beginParse()

fun ByteArray.toSnakeData(): SnakeData {
    val chunks = this.asList().chunked(127)

    if (chunks.isEmpty())
        return SnakeDataTail(BitString.empty())

    if (chunks.size == 1)
        return SnakeDataTail(BitString.of(chunks[0].toByteArray()))

    var nextSnakeData: SnakeData = SnakeDataTail(BitString.of(chunks.last().toByteArray()))

    for (i in (0..chunks.size - 2).reversed()) {
        val chunk = chunks[i]

        val currSnakeData = SnakeDataCons(BitString.of(chunk.toByteArray()), nextSnakeData)
        nextSnakeData = currSnakeData
    }

    return nextSnakeData
}

fun Cell.parseSnakeData(): String {
    var c: Cell? = this

    val bitResult: MutableList<BitString> = mutableListOf()

    while (c != null) {
        val cs = c.beginParse()
        if (cs.remainingBits == 0)
            break

        val data = cs.loadBits(cs.remainingBits)
        bitResult.add(data)

        c = if (c.refs.isNotEmpty()) c.refs[0] else null
    }

    return bitResult.joinToString("") { String(it.toByteArray()) }
}

fun SnakeData.flatten(): ByteArray = when (this) {
    is SnakeDataTail -> bits.toByteArray()
    is SnakeDataCons -> bits.toByteArray() + next.flatten()
}

fun ContentData.flatten(): ByteArray = when (this) {
    is ContentData.Snake -> this.data.flatten()
    is ContentData.Chunks -> TODO("chunky content data")
}


fun <T> Flow<T>.handleErrors(logger: Logger, stacktrace: Boolean = false): Flow<T> =
    catch { e ->
        logger.error(e.message)
        if (stacktrace) logger.error(e.stackTraceToString())
    }

fun String.binStringToBitString() = BitString(this.toBinaryArray())

fun LiteServerAccountId(address: AddrStd) = LiteServerAccountId(address.workchainId, address.address.toByteArray())


suspend fun LiteClient.getAccount(s: String): AccountInfo? {
    val account = this.getAccountState(AddrStd(s)).account.value
    return when (account) {
        is AccountInfo -> account
        is AccountNone -> null
        else -> null
    }
}

fun List<Pair<String, Long>>.toWalletTransfer() = this.map {
    WalletTransfer {
        destination = AddrStd(it.first)
        coins = Coins.ofNano(it.second)
        bounceable = false
        sendMode = SendMode.PAY_GAS_SEPARATELY
    }
}
