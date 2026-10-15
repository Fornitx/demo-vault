package com.example.demo.vault.db

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.boot.jooq.autoconfigure.JooqProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DemoDbConfig(private val jooqProperties: JooqProperties) {
    @Bean
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext {
        return DSL.using(connectionFactory, jooqProperties.sqlDialect)
    }
}