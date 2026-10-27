package com.example.demo.vault.vault.test

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

private val log = KotlinLogging.logger {}

class DemoQuartzInitTest {
    @Test
    fun test() {
        val sql = ClassPathResource("org/quartz/impl/jdbcjobstore/tables_postgres.sql")
            .getContentAsString(StandardCharsets.UTF_8)
        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            for (port in listOf(5432, 5433)) {
                executor.execute {
                    initDB(port, sql)
                }
            }
        }
    }

    private fun initDB(port: Int, sql: String) {
        log.info { "Start initDB: $port" }

        val jdbcTemplate = JdbcTemplate(
            DataSourceBuilder.create()
                .type(SimpleDriverDataSource::class.java)
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost:${port}/${Constants.DATABASE}")
                .username(Constants.USERNAME)
                .password(Constants.PASSWORD)
                .build()
        )
        val rowUpdated = jdbcTemplate.update(sql)
        log.info { "End initDB: $port, $rowUpdated" }
    }
}