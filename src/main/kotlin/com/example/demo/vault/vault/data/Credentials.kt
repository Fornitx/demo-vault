package com.example.demo.vault.vault.data

data class Credentials(
    val username: String,
    val password: String,
    val ttl: Long,
)