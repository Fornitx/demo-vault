package com.example.demo.vault.vault

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vault")
data class DemoVaultProperties(
    val url: String,
    val token: String,
    val path: String,
)