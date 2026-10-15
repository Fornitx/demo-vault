package com.example.demo.vault.rest.clients

import org.springframework.cloud.vault.config.VaultProperties
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.client.toEntity
import reactor.core.publisher.Mono

private const val X_VAULT_TOKEN = "X-Vault-Token"

@Component
class DemoVaultClient(vaultProperties: VaultProperties, webClientBuilder: WebClient.Builder) {
    private val webClient = webClientBuilder.baseUrl(vaultProperties.uri!!)
        .defaultHeader(X_VAULT_TOKEN, vaultProperties.token!!)
        .build()

    fun getDataRaw(): Mono<ResponseEntity<String>> {
        return webClient.get()
            .uri("/v1/secret/data/myapp")
            .exchangeToMono { it.toEntity<String>() }
    }

    fun getData(): Mono<Map<Any, Any>> {
        return webClient.get()
            .uri("/v1/secret/data/myapp")
            .exchangeToMono { it.bodyToMono<VaultResponse>() }
            .map { it.data.data }
    }

    fun setPassword(password: String): Mono<Void> {
        return webClient.post()
            .uri("/v1/secret/data/myapp")
            .bodyValue(VaultData(mapOf("password" to password)))
            .exchangeToMono { it.bodyToMono<String>() }
            .then()
    }

    private class VaultResponse(val data: VaultData)

    private class VaultData(val data: Map<Any, Any>)
}
