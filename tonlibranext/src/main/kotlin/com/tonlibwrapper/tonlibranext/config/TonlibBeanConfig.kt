package com.tonlibwrapper.tonlibranext.config

import com.tonlibwrapper.tonlibranext.client.TonClient
import com.tonlibwrapper.tonlibranext.common.ipv4IntToStr
import com.tonlibwrapper.tonlibranext.common.utcNow
import com.tonlibwrapper.tonlibranext.common.utcTsNow
import com.tonlibwrapper.tonlibranext.mapper.TonMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.api.liteserver.LiteServerDesc
import org.ton.api.pub.PublicKeyEd25519
import org.ton.api.validator.config.ValidatorConfigGlobal
import org.ton.crypto.encoding.base64
import org.ton.lite.client.LiteClient

@Configuration
class TonlibBeanConfig(
    @Value("\${spring.profiles.active:local}")
    private val activeProfile: String
) {

    @Bean("tonlibra-config-reader")
    fun tonConfig() = TonChainConfigReader()

    @Bean
    fun tonClient(): TonClient = TonClient(liteClient(tonConfig()))

    @Bean
    fun tonlibMapper(): TonMapper = TonMapper()

//    val allowedTimeoutMs = if (activeProfile != "prod") 450 else 700
    val allowedTimeoutMs = 750

    @Bean
    fun liteClient(tonChainConfigReader: TonChainConfigReader): LiteClient {
        val configList = tonChainConfigReader.load().liteservers
        val nearestNodesList = mutableListOf<TonChainConfigReader.TonNetConfig.LiteServerParams>()

        configList.forEach {
            val liteClient = LiteClient(
                coroutineContext = Dispatchers.Default,
                liteClientConfigGlobal = LiteClientConfigGlobal(
                    liteServers = listOf(
                        LiteServerDesc(id = PublicKeyEd25519(base64(it.id.key)), ip = it.ip, port = it.port)
                    ),
                    validator = ValidatorConfigGlobal()
                )
            )

            kotlin.runCatching {
                runBlocking {
                    try {
                        println("${utcNow()} NODECHECK ${it.ip} (${ipv4IntToStr(it.ip)})")

                        val lastBlockId = liteClient.getLastBlockId()
                        val start = utcTsNow()

                        liteClient.getBlock(lastBlockId)

                        val delay = utcTsNow().time - start.time
                        println("${utcNow()} NODECHECK ${it.ip} (${ipv4IntToStr(it.ip)}) getLastBlock took $delay ms")
                        if (delay < allowedTimeoutMs)
                            nearestNodesList.add(it)
                    } catch (ex: Exception) {
                        println(ex.message)
                        println("${utcNow()} NODECHECK ${it.ip} (${ipv4IntToStr(it.ip)}) FAIL")
                    }
                }
            }
        }
        println("NODECHECK: suggested to use ${nearestNodesList.size}/${configList.size} liteservers")

//        nearestNodesList.add(
//                TonChainConfigReader.TonNetConfig.LiteServerParams(
//                        ip = 2018135749,
//                        port = 53312,
//                        id = TonChainConfigReader.TonNetConfig.LiteServerId(
//                                key = "aF91CuUHuuOv9rm2W5+O/4h38M3sRm40DtSdRxQhmtQ=",
//                                type = "pub.ed25519"
//                        )
//                )
//        )

        return LiteClient(
            coroutineContext = Dispatchers.Default,
            liteClientConfigGlobal = LiteClientConfigGlobal(
                liteServers = (nearestNodesList.takeIf { it.isNotEmpty() } ?: configList ).map {
                    LiteServerDesc(id = PublicKeyEd25519(base64(it.id.key)), ip = it.ip, port = it.port)
                },
                validator = ValidatorConfigGlobal()
            )
        )
    }

}
