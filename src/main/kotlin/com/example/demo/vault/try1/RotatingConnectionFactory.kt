package com.example.demo.vault.try1

import io.r2dbc.spi.*
import org.reactivestreams.Publisher
import org.springframework.boot.r2dbc.autoconfigure.R2dbcProperties

class RotatingConnectionFactory(
    private val r2dbcProperties: R2dbcProperties,
    private val credentialsProvider: CredentialsProvider,
) : ConnectionFactory {
    init {
        credentialsProvider.setCredentials("pguser", "pgpass")
    }

    override fun create(): Publisher<out Connection?> {
        val creds = credentialsProvider.getCredentials()

        val options = ConnectionFactoryOptions.parse(r2dbcProperties.url!!)
            .mutate()
            .option(ConnectionFactoryOptions.USER, creds.username!!)
            .option(ConnectionFactoryOptions.PASSWORD, creds.password!!)
            .build()

        return ConnectionFactories.get(options).create()
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        val creds = credentialsProvider.getCredentials()

        val options = ConnectionFactoryOptions.parse(r2dbcProperties.url!!)
            .mutate()
            .option(ConnectionFactoryOptions.USER, creds.username!!)
            .option(ConnectionFactoryOptions.PASSWORD, creds.password!!)
            .build()

        return ConnectionFactories.get(options).metadata
    }
}