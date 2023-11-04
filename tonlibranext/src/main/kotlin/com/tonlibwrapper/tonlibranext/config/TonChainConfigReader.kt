package com.tonlibwrapper.tonlibranext.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.tonlibwrapper.tonlibranext.common.enums.TonNetworkMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import java.nio.charset.Charset

@Component
class TonChainConfigReader {

    private val logger = LoggerFactory.getLogger(this::class.simpleName)

    @Value("\${ton.net-config}")
    private lateinit var networkMode: TonNetworkMode

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TonNetConfig (
        @JsonProperty("liteservers")
        val liteservers: List<LiteServerParams>
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class LiteServerParams (
            @JsonProperty("ip")
            val ip: Int,
            @JsonProperty("port")
            val port: Int,
            @JsonProperty("id")
            val id: LiteServerId
        )
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class LiteServerId (
            @JsonProperty("@type")
            val type: String,
            @JsonProperty("key")
            val key: String
        )
    }

    fun load(): TonNetConfig {
        val configFile = when (networkMode) {
            TonNetworkMode.MAINNET -> ClassPathResource("./global-config.json")
            TonNetworkMode.TESTNET -> ClassPathResource("./testnet-global.config.json")
        }
        logger.info("Starting service in $networkMode mode")
        return ObjectMapper().readValue(
            StreamUtils.copyToString(configFile.inputStream, Charset.defaultCharset()),
            TonNetConfig::class.java
        )
    }
}
