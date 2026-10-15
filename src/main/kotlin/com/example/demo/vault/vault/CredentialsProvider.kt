package com.example.demo.vault.vault

import io.github.oshai.kotlinlogging.KotlinLogging
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.ConnectionFactory
import java.util.concurrent.atomic.AtomicReference

class CredentialsProvider(initialCreds: Credentials, private val connectionFactory: ConnectionFactory) {
    private val log = KotlinLogging.logger {}

    private val credentialsRef = AtomicReference<Credentials>(initialCreds)

    fun getCredentials(): Credentials {
        return credentialsRef.get()
    }

    fun setCredentials(credentials: Credentials) {
        val currentCreds = credentialsRef.get()
        if (currentCreds != credentials) {
            log.info { "Setting credentials: $credentials" }
            credentialsRef.set(credentials)

            if (connectionFactory is ConnectionPool) {
                connectionFactory.close().subscribe()
            }
        }
    }
}
