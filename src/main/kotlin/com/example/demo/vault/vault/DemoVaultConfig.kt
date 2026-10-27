package com.example.demo.vault.vault

import com.example.demo.vault.vault.providers.CachedCredentialsProvider
import com.example.demo.vault.vault.providers.CredentialsProvider
import com.example.demo.vault.vault.providers.ListenableCredentialsProvider
import com.example.demo.vault.vault.providers.UpdatableCredentialsProvider
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import org.springframework.util.StringUtils
import javax.sql.DataSource

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
    fun jdbcConnectionDetails(
        properties: DataSourceProperties,
        credentialsProvider: CredentialsProvider,
    ): JdbcConnectionDetails {
        return object : JdbcConnectionDetails {
            override fun getUsername(): String = credentialsProvider.getCredentials().username

            override fun getPassword(): String = credentialsProvider.getCredentials().password

            override fun getJdbcUrl(): String = properties.url!!
        }
    }

    @Bean
    fun dataSource(
        properties: DataSourceProperties,
        connectionDetails: JdbcConnectionDetails,
        environment: Environment,
        credentialsProvider: ListenableCredentialsProvider,
    ): DataSource {
        val dataSourceClassName = environment.getProperty("spring.datasource.hikari.data-source-class-name")
        val dataSource = createDataSource<DataSource>(
            connectionDetails,
            properties.type,
            properties.classLoader,
            dataSourceClassName == null,
        )

        when (dataSource) {
            is HikariDataSource -> {
                if (StringUtils.hasText(properties.name)) {
                    dataSource.setPoolName(properties.name)
                }
                credentialsProvider.addCredentialsListener { credentials ->
                    dataSource.password = credentials.password
                    dataSource.hikariPoolMXBean.softEvictConnections()
                }
            }

            is SimpleDriverDataSource -> {
                credentialsProvider.addCredentialsListener { credentials ->
                    dataSource.password = credentials.password
                }
            }
        }

        return dataSource
    }

    private fun <T : DataSource> createDataSource(
        connectionDetails: JdbcConnectionDetails,
        type: Class<out DataSource>?,
        classLoader: ClassLoader,
        applyDriverClassName: Boolean,
    ): T {
        val builder = DataSourceBuilder.create(classLoader).type(type)
        if (applyDriverClassName) {
            builder.driverClassName(connectionDetails.getDriverClassName())
        }
        return builder.url(connectionDetails.jdbcUrl)
            .username(connectionDetails.username)
            .password(connectionDetails.password)
            .build() as T
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @ConditionalOnBooleanProperty("management.endpoint.vault.enabled")
    fun vaultEndpoint(credentialsProvider: UpdatableCredentialsProvider): DemoVaultEndpoint {
        return DemoVaultEndpoint(credentialsProvider)
    }
}