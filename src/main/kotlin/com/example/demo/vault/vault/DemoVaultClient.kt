package com.example.demo.vault.vault

import org.springframework.vault.authentication.SimpleSessionManager
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.RestClientBuilder
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultTemplate

class DemoVaultClient(private val properties: DemoVaultProperties) {
    private val vaultTemplate = VaultTemplate(
        RestClientBuilder.builder().endpoint(VaultEndpoint.from(properties.url)),
        SimpleSessionManager(TokenAuthentication(properties.token)),
    )

    fun getDataRaw(): Map<String, Any> {
        return vaultTemplate.readRequired(properties.path).requiredData
    }

    fun getData(): VaultOuterData {
        return vaultTemplate.readRequired(properties.path, VaultOuterData::class.java).requiredData
    }

    data class VaultOuterData(val data: Credentials)
}
