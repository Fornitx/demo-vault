package com.example.demo.vault.vault

import com.example.demo.vault.vault.providers.UpdatableCredentialsProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation

private val log = KotlinLogging.logger {}

@Endpoint(id = "vault")
class DemoVaultEndpoint(private val credentialsProvider: UpdatableCredentialsProvider) {
    @WriteOperation
    fun updateCredentials() {
        log.info { "Updating credentials via /actuator/vault" }
        credentialsProvider.update()
    }
}