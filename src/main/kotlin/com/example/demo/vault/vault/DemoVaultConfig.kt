package com.example.demo.vault.vault

import com.example.demo.vault.vault.providers.CachedCredentialsProvider
import com.example.demo.vault.vault.providers.CredentialsProvider
import com.example.demo.vault.vault.providers.UpdatableCredentialsProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.r2dbc.autoconfigure.ConnectionFactoryOptionsBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

private val log = KotlinLogging.logger {}

@Configuration
@ConditionalOnBooleanProperty("vault.enabled")
class DemoVaultConfig {
    @Bean
    fun demoVaultClient(vaultProperties: DemoVaultProperties): DemoVaultClient {
        return DemoVaultClient(vaultProperties)
    }

    @Bean
    fun credentialsProvider(vaultClient: DemoVaultClient): CachedCredentialsProvider {
        return CachedCredentialsProvider { vaultClient.getCredentials() }
    }

    @Bean
    fun credentialsProviderConnectionFactoryOptionsBuilderCustomizer(
        credentialsProvider: CredentialsProvider,
    ): ConnectionFactoryOptionsBuilderCustomizer {
        return ConnectionFactoryOptionsBuilderCustomizer { builder ->
            builder.option(
                Option.valueOf(ConnectionFactoryOptions.USER.name()),
                Supplier { credentialsProvider.getCredentials().username },
            ).option(
                Option.sensitiveValueOf(ConnectionFactoryOptions.PASSWORD.name()),
                Supplier { credentialsProvider.getCredentials().password },
            )
        }
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @ConditionalOnBooleanProperty("management.endpoint.vault.enabled")
    fun vaultEndpoint(credentialsProvider: UpdatableCredentialsProvider): DemoVaultEndpoint {
        return DemoVaultEndpoint(credentialsProvider)
    }
}