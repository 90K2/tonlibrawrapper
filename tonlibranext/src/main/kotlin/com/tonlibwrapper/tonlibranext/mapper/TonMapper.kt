package com.tonlibwrapper.tonlibranext.mapper

import com.tonlibwrapper.tonlibranext.common.dto.AccountResponseDTO
import com.tonlibwrapper.tonlibranext.constants.OpCodes
import com.tonlibwrapper.tonlibranext.dto.*
import com.tonlibwrapper.tonlibranext.getState
import com.tonlibwrapper.tonlibranext.loadRemainingBits
import com.tonlibwrapper.tonlibranext.parseSnakeData
import com.tonlibwrapper.tonlibranext.toAddrString
import org.springframework.stereotype.Component
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.crypto.hex
import org.ton.tlb.CellRef
import org.ton.tlb.loadTlb

@Component
class TonMapper {

    fun toAccountDTO(s: AccountInfo?): AccountResponseDTO? {
        if (s == null) return null
        val storageInit = s.storage.state.getState()
        return AccountResponseDTO(
            address = AddrStd.toString(s.addr as AddrStd),
            addressRaw = (s.addr as AddrStd).address.toString(),
            status = s.storage.state.toString(),
            balance = s.storage.balance.coins.amount.toLong(),
            code = storageInit?.code?.value.toString(),
            data = storageInit?.data?.value.toString()
        )
    }

    private fun readComputePhase(t: TransOrd?): TrPhaseComputeVm? {
        if (t == null) return null
        return when (t.computePh) {
            is TrPhaseComputeVm -> t.computePh as TrPhaseComputeVm
            else -> null
        }
    }

    private fun readTxDescr(d: TransactionDescr): TransOrd? {
        return when (d) {
            is TransOrd -> d
            else -> null
        }
    }

    private fun mapMsg(
        info: CommonMsgInfo?,
        body: Either<Cell, Cell>?,
        init: Maybe<Either<StateInit, StateInit>>?,
        msgType: TonMsgType
    ): TonTxMsg? {
        if (info == null) return null
        val bodyValue = body?.toPair()?.toList()?.filter { it != null && !it.isEmpty() }?.firstOrNull()

        var msgAction = when {
            (msgType.inMsg() && body?.x != null && body.x?.isEmpty() == false) -> TonMsgAction.INVOCATION
            ( init?.value == null && bodyValue == null) -> TonMsgAction.TRANSFER
            (msgType.inMsg() && init != null) -> TonMsgAction.INIT
            else -> TonMsgAction.TRANSFER
        }
        var comment: String? = null
        var opCode: Long? = null
        var ftAmount: Long? = null
        var newContractOwner: AddrStd? = null

        bodyValue?.parse {
            if (this.bits.size >= 32) {
                val tag = loadUInt(32).toLong()
                if (tag == 0xd53276db || tag == 0L) {
                    msgAction = TonMsgAction.TRANSFER
                    comment = bodyValue.parseSnakeData()
                }
                if (tag == OpCodes.OP_SFT_MINTER_MINT.toLong())
                    msgAction = TonMsgAction.INVOCATION
                if (tag == OpCodes.OP_WALLET_INTERTRANSFER.toLong() || tag == OpCodes.OP_SFT_TRANSFER_NOTIFICATION.toLong()) {
                    loadUInt(64) // skip queryId
                    ftAmount = loadTlb(Coins.tlbCodec()).amount.toLong()
                }
                if (tag == OpCodes.OP_NFT_TRANSFER.toLong()) {
                    loadUInt(64) // skip queryId
                    newContractOwner = loadTlb(MsgAddressInt.tlbCodec()) as AddrStd
                }
                opCode = tag
            }
            loadRemainingBits()
        }

        if (info is IntMsgInfo)
            return TonTxMsg(
                value = info.value.coins.amount.toLong(),
                fwdFee = info.fwd_fee.amount.toLong(),
                ihrFee = info.ihr_fee.amount.toLong(),
                source = info.src.toAddrString(),
                destination = info.dest.toAddrString(),
                createdLt = info.created_lt,
                op = opCode,
                init = init?.value != null,
                msgAction = msgAction,
                msgType = msgType,
                comment = comment,
                payload = TonTxMsgPayload(
                    transferAmount = ftAmount,
                    newContractOwner = newContractOwner?.toAddrString()
                )
            )

        return null
    }

    fun <X> Either<X, CellRef<X>>?.trimCellRef(): Either<X, X>? {
        if (this == null) return null
//        if (this.x == null || this.y?.value == null) return null
        return Either.of(this.x, this.y?.value)
    }

    private fun mapMsgIn(m: Maybe<Message<Cell>>) = mapMsg(
        m.value?.info,
        m.value?.body?.trimCellRef(),
        Maybe.of(m.value?.init?.value?.trimCellRef()),
        TonMsgType.IN
    )
    private fun mapMsgOut(m: Message<Cell>) = mapMsg(
        m.info,
        m.body.trimCellRef(),
        Maybe.of(m.init.value?.trimCellRef()),
        TonMsgType.OUT
    )


    fun mapTx(tx: Transaction, blockId: Int, wc: Int): TonTxDTO {
        val descr = readTxDescr(tx.description.value)
        val computePh = readComputePhase(descr)
        return TonTxDTO(
            lt = tx.lt.toLong(),
            blockId = blockId,
            account = tx.accountAddr,
            accountAddr = AddrStd(wc, tx.accountAddr).toAddrString(),
            hash = hex(tx.hash().toByteArray()),
            gasFee = tx.totalFees.coins.amount.toLong(),
            actionFwdFee = descr?.action?.value?.value?.totalFwdFees?.value?.amount?.toLong(),
            actionFee = descr?.action?.value?.value?.totalActionFees?.value?.amount?.toLong(),
            storageFee = descr?.storagePh?.value?.storageFeesCollected?.amount?.toLong(),
            computeFee = computePh?.gasFees?.amount?.toLong(),
            workchain = wc,
            init = tx.origStatus != AccountStatus.ACTIVE && tx.endStatus == AccountStatus.ACTIVE,
            inMsg = mapMsgIn(Maybe.of(tx.r1.value.inMsg.value?.value)),
            outMsg = tx.r1.value.outMsgs.toMap().entries.mapNotNull { mapMsgOut(it.value.value) },
            computeSucceed = computePh?.success,
            computeExitCode = computePh?.r1?.value?.exitCode,
            actionSucceed = descr?.action?.value?.value?.success,
            actionExitCode = descr?.action?.value?.value?.resultCode
        )
    }

}

