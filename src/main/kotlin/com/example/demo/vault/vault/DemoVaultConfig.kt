package com.example.demo.vault.vault

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.r2dbc.autoconfigure.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono

@Configuration
@ConditionalOnBooleanProperty("vault.enabled")
class DemoVaultConfig(
    private val vaultProperties: DemoVaultProperties,
) {
    @Bean
    fun demoVaultClient(): DemoVaultClient {
        return DemoVaultClient(vaultProperties)
    }

    @Bean
    fun credentialsProvider(connectionFactory: ConnectionFactory): CredentialsProvider {
        return CredentialsProvider(demoVaultClient().getData().data)
    }

    @Bean
    @Primary
    fun connectionFactory(properties: R2dbcProperties): ConnectionFactory {
        return RotatingConnectionFactory(properties, credentialsProvider())
    }

    private fun fetchFreshUsername(): Mono<String> {
        return Mono.fromCallable { credentialsProvider().getCredentials().username }
    }

    private fun fetchFreshPassword(): Mono<CharSequence> {
        return Mono.fromCallable { credentialsProvider().getCredentials().password }
    }
}