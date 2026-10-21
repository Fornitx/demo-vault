package com.example.demo.vault.vault.providers

import com.example.demo.vault.vault.data.Credentials

fun interface CredentialsProvider {
    fun getCredentials(): Credentials
}