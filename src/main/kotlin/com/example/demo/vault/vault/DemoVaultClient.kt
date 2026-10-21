package com.example.demo.vault.vault

import com.example.demo.vault.vault.data.Credentials
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.vault.authentication.SimpleSessionManager
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.RestClientBuilder
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultTemplate

private val log = KotlinLogging.logger {}

class DemoVaultClient(private val properties: DemoVaultProperties) {
    private val vaultTemplate = VaultTemplate(
        RestClientBuilder.builder().endpoint(VaultEndpoint.from(properties.url)),
        SimpleSessionManager(TokenAuthentication(properties.token)),
    )

    fun getDataRaw(): Map<String, Any> {
        log.info { "DemoVaultClient.getDataRaw() called" }
        return vaultTemplate.readRequired(properties.path).requiredData
    }

    fun getCredentials(): Credentials {
        log.info { "DemoVaultClient.getCredentials() called" }
        return vaultTemplate.readRequired(properties.path, VaultOuterData::class.java).requiredData.data
    }

    data class VaultOuterData(val data: Credentials)
}
