package com.tonlibwrapper.tonlibranext

import com.tonlibwrapper.tonlibranext.common.Extensions.toBinary
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import kotlin.math.ceil
import kotlin.math.log2

enum class LabelType {SHORT, LONG, SAME}

enum class HMNodeType { LEAF, FORK }

interface HMNode<T> { val type: com.tonlibwrapper.tonlibranext.HMNodeType }

data class HMNodeLeaf<T>(
    val value: T,
    override val type: com.tonlibwrapper.tonlibranext.HMNodeType = com.tonlibwrapper.tonlibranext.HMNodeType.LEAF
): com.tonlibwrapper.tonlibranext.HMNode<T>

data class HMNodeFork<T>(
    override val type: com.tonlibwrapper.tonlibranext.HMNodeType = com.tonlibwrapper.tonlibranext.HMNodeType.FORK,
    val left: com.tonlibwrapper.tonlibranext.HMEdge<T>,
    val right: com.tonlibwrapper.tonlibranext.HMEdge<T>
): com.tonlibwrapper.tonlibranext.HMNode<T>

data class HMEdge<T> (
    val label: String,
    val node: com.tonlibwrapper.tonlibranext.HMNode<T>
)


    private fun pad(src: String, size: Int): String {
        var t = src
        while (t.length < size) {
            t = "0$t"
        }
        return t
    }

    private fun <T> forkMap(src: Map<String, T>): Pair<Map<String, T>, Map<String, T>> {
        require(src.isNotEmpty())
        val left = src.filterKeys { it.startsWith("0") }.mapKeys { it.key.drop(1) }
        val right = src.filterKeys { it.startsWith("1") }.mapKeys { it.key.drop(1) }
        require(left.isNotEmpty())
        require(right.isNotEmpty())

        return Pair(left, right)
    }

    private fun <T> buildNode(src: Map<String, T>): com.tonlibwrapper.tonlibranext.HMNode<T> {
        require(src.isNotEmpty())

        return if (src.size == 1) {
            com.tonlibwrapper.tonlibranext.HMNodeLeaf(value = src.values.first())
        } else {
            val (left, right) = com.tonlibwrapper.tonlibranext.forkMap(src)
            com.tonlibwrapper.tonlibranext.HMNodeFork(
                left = com.tonlibwrapper.tonlibranext.buildEdge(left),
                right = com.tonlibwrapper.tonlibranext.buildEdge(right)
            )
        }
    }

    private fun <T> buildEdge(src: Map<String, T>): com.tonlibwrapper.tonlibranext.HMEdge<T> {
        require(src.isNotEmpty())

        // common prefix
        val label = src.keys.toList()
            .reduce { acc, string ->
                acc.zip(string)
                    .takeWhile { it.first == it.second }
                    .map { it.first }
                    .joinToString(separator = "")
            }
        return com.tonlibwrapper.tonlibranext.HMEdge(
            label = label,
            node = com.tonlibwrapper.tonlibranext.buildNode(src.mapKeys { it.key.drop(label.length) })
        )
    }

    fun <T> buildTree(src: Map<Int, T>, keyLength: Int): com.tonlibwrapper.tonlibranext.HMEdge<T> {
        return com.tonlibwrapper.tonlibranext.buildEdge(src.mapKeys {
            com.tonlibwrapper.tonlibranext.pad(
                it.key.toBinary(
                    2
                ), keyLength
            )
        })
    }

    //
    // Serialization
    //

    fun writeLabelShort(src: String): BitString {
        var to = BitString()
        // header
        to += BitString(false)

        // unary length
        repeat((src.indices).count()) { to += BitString(true) }
        to += BitString(false)

        // value
        repeat((src.indices).count()) { to += BitString(src[it] == "1".first()) }

        return to
    }

    private fun labelShortLength(src: String) = 1 + src.length + 1 + src.length

    fun writeLabelLong(src: String, keyLength: Int): BitString {
        var to = BitString()
        // header
        to += BitString(true, false)

        // length
        val len = ceil(log2(keyLength + 1.0)).toInt()
        to += com.tonlibwrapper.tonlibranext.writeUint(src.length, len)

        // value
        repeat((src.indices).count()) { to += BitString(src[it] == "1".first()) }

        return to
    }

    private fun labelLongLength(src: String, keyLength: Int) =
        (1 + 1 + ceil(log2(keyLength + 1.0)).toInt() + src.length).toInt()

    fun writeLabelSame(value: Boolean, len: Int, keyLength: Int): BitString {
        var to = BitString()
        // header
        to += BitString(true, true)

        // value
        to += BitString(value)

        // length
        val lenlen = ceil(log2(keyLength + 1.0)).toInt()
        to += com.tonlibwrapper.tonlibranext.writeUint(len, lenlen)

        return to
    }

    private fun labelSameLength(keyLength: Int) =
        (1 + 1 + 1 + ceil(log2(keyLength + 1.0))).toInt()

    private fun isSame(src: String): Boolean {
        if (src.isEmpty() || src.length == 1)
            return true
        (1 until src.length).forEach {
            if (src[it] != src.first())
                return false
        }
        return true
    }

    fun detectLabelType(src: String, keyLength: Int): com.tonlibwrapper.tonlibranext.LabelType {
        var type = com.tonlibwrapper.tonlibranext.LabelType.SHORT
        var labelLen = com.tonlibwrapper.tonlibranext.labelShortLength(src)

        val longLen = com.tonlibwrapper.tonlibranext.labelLongLength(src, keyLength)
        if (longLen < labelLen) {
            labelLen = longLen
            type = com.tonlibwrapper.tonlibranext.LabelType.LONG
        }

        if (com.tonlibwrapper.tonlibranext.isSame(src)) {
            val sameLen = com.tonlibwrapper.tonlibranext.labelSameLength(keyLength)
            if (sameLen < labelLen) {
                labelLen = sameLen
                type = com.tonlibwrapper.tonlibranext.LabelType.SAME
            }
        }

        return type
    }

    fun writeLabel(src: String, keyLength: Int): BitString {
        val type = com.tonlibwrapper.tonlibranext.detectLabelType(src, keyLength)
        val r = when (type) {
            com.tonlibwrapper.tonlibranext.LabelType.SHORT -> com.tonlibwrapper.tonlibranext.writeLabelShort(src)
            com.tonlibwrapper.tonlibranext.LabelType.LONG -> com.tonlibwrapper.tonlibranext.writeLabelLong(
                src,
                keyLength
            )
            com.tonlibwrapper.tonlibranext.LabelType.SAME -> com.tonlibwrapper.tonlibranext.writeLabelSame(
                src.first() == "1".first(),
                src.length,
                keyLength
            )
        }

        return r
    }

    fun <T> writeNode(src: com.tonlibwrapper.tonlibranext.HMNode<T>, keyLength: Int, serializer: (src: T, cell: CellBuilder) -> Unit, to: CellBuilder): Cell {
        if (src.type == com.tonlibwrapper.tonlibranext.HMNodeType.LEAF)
            serializer( (src as com.tonlibwrapper.tonlibranext.HMNodeLeaf).value, to)

        if (src.type == com.tonlibwrapper.tonlibranext.HMNodeType.FORK) {
            (src as com.tonlibwrapper.tonlibranext.HMNodeFork)
            to.storeRef(
                com.tonlibwrapper.tonlibranext.writeEdge(src.left, keyLength - 1, serializer)
            )
            to.storeRef(
                com.tonlibwrapper.tonlibranext.writeEdge(src.right, keyLength - 1, serializer)
            )
        }

        return to.endCell()
    }

    fun <T> writeEdge(src: com.tonlibwrapper.tonlibranext.HMEdge<T>, keyLength: Int, serializer: (src: T, cell: CellBuilder) -> Unit): Cell {
        val to = CellBuilder.beginCell()
        to.storeBits(com.tonlibwrapper.tonlibranext.writeLabel(src.label, keyLength))
        com.tonlibwrapper.tonlibranext.writeNode(src.node, keyLength, serializer, to)

        return to.endCell()
    }

    fun <T> serializeMap(src: Map<Int, T>, keyLength: Int, serializer: (src: T, cb: CellBuilder) -> Unit): Cell {
        return com.tonlibwrapper.tonlibranext.writeEdge(
            com.tonlibwrapper.tonlibranext.buildTree(src, keyLength),
            keyLength,
            serializer
        )
    }

fun writeUint(value: Int, n: Int): BitString {
    var to = BitString()
    val s = value.toBinary(n)
    (0 until n).forEach { to += BitString(s[it] == "1".first()) }
    return to
}