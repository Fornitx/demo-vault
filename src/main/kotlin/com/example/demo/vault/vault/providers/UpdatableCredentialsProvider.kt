package com.example.demo.vault.vault.providers

interface UpdatableCredentialsProvider : CredentialsProvider {
    fun update()
}