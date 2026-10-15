package com.example.demo.vault.db

import com.example.demo.vault.try1.CredentialsProvider
import com.example.demo.vault.try1.RotatingConnectionFactory
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.boot.jooq.autoconfigure.JooqProperties
import org.springframework.boot.r2dbc.autoconfigure.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration

@Configuration
class DemoConfig(
    private val r2dbcProperties: R2dbcProperties,
    private val jooqProperties: JooqProperties,
) {
    @Bean
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext {
        return DSL.using(connectionFactory, jooqProperties.sqlDialect)
    }

    @Bean
    @Primary
    fun connectionFactory(credentialsProvider: CredentialsProvider): ConnectionFactory {
        val connectionFactory = RotatingConnectionFactory(r2dbcProperties, credentialsProvider)

        val poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
            .initialSize(1)
            .maxSize(5)
            .maxIdleTime(Duration.ofSeconds(10))
            .maxLifeTime(Duration.ofSeconds(20))
            .validationQuery("select 1")
            .build()

        return ConnectionPool(poolConfig)
    }
}