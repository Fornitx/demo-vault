package com.example.demo.vault

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.boot.jooq.autoconfigure.JooqProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DemoConfig {
    @Bean
    fun dslContext(connectionFactory: ConnectionFactory, jooqProperties: JooqProperties): DSLContext {
        return DSL.using(connectionFactory, jooqProperties.sqlDialect)
    }
}