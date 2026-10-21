package com.example.demo.vault.vault.test

import io.github.oshai.kotlinlogging.KotlinLogging
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.r2dbc.core.DatabaseClient

private val log = KotlinLogging.logger {}

@Deprecated("")
object DemoDbPasswordChanger {
    fun changePassword(old: String, new: String) {
        runBlocking {
            val deferreds = listOf(5432, 5433).map { port -> async { changePasswordForInstance(port, old, new) } }
            deferreds.awaitAll()
        }
    }

    private suspend fun changePasswordForInstance(port: Int, old: String, new: String) {
        log.info { "Start changePasswordForInstance: $port" }
        val connectionFactory = ConnectionFactories.get(
            ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.PORT, port)
                .option(ConnectionFactoryOptions.DATABASE, Constants.DATABASE)
                .option(ConnectionFactoryOptions.USER, Constants.USERNAME)
                .option(ConnectionFactoryOptions.PASSWORD, old)
                .build()
        )
        val databaseClient = DatabaseClient.create(connectionFactory)
        val rowUpdated = databaseClient.sql { "alter user current_user with password '$new'" }
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        log.info { "End changePasswordForInstance: $port, $rowUpdated" }
    }
}