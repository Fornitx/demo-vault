package com.example.demo.vault.vault.test

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.util.concurrent.Executors

private val log = KotlinLogging.logger {}

@Deprecated("")
object DemoDbPasswordChanger {
    fun changePassword(old: String, new: String) {
        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            for (port in listOf(5432, 5433)) {
                executor.execute {
                    changePasswordForInstance(port, old, new)
                }
            }
        }
    }

    private fun changePasswordForInstance(port: Int, old: String, new: String) {
        log.info { "Start changePasswordForInstance: $port" }

        val jdbcTemplate = JdbcTemplate(
            DataSourceBuilder.create()
                .type(SimpleDriverDataSource::class.java)
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost:${port}/${Constants.DATABASE}")
                .username(Constants.USERNAME)
                .password(old)
                .build()
        )
        val rowUpdated = jdbcTemplate.update("alter user current_user with password '$new'")
        log.info { "End changePasswordForInstance: $port, $rowUpdated" }
    }
}