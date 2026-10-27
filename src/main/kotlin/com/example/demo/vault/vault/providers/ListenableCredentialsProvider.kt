package com.example.demo.vault.vault.providers

interface ListenableCredentialsProvider : CredentialsProvider {
    fun addCredentialsListener(credentialsListener: CredentialsListener)
}