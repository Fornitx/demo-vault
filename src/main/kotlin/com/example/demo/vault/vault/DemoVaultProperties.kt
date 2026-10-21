package com.example.demo.vault.vault

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vault", ignoreUnknownFields = false)
data class DemoVaultProperties(
    val enabled: Boolean,
    val url: String,
    val token: String,
    val path: String,
)