package com.example.demo.vault.db

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private const val QUERY =
    "select concat_ws(' | ', pg_backend_pid(), (select system_identifier from pg_control_system()), version())"

@Component
class DemoDao(
    private val dslContext: DSLContext,
    private val connectionFactory: ConnectionFactory,
) {
//    fun getFooJdbc(): String {
//        val result = dslContext.fetch(idQuery)
//        val strings = result.into(String::class.java)
//        return strings.toString()
//    }

    fun getFooR2dbc(): Mono<String> {
        return Mono.from(dslContext.resultQuery(QUERY))
            .map { it.into(String::class.java) }
    }

    fun getFooRaw(): Mono<String> {
        return Flux.usingWhen(
            connectionFactory.create(),

            { connection ->
                Flux.from(connection.createStatement(QUERY).execute())
                    .flatMap { result ->
                        result.map<String> { row, metadata ->
                            row.get(0, String::class.java)
                        }
                    }
            },

            Connection::close,
        ).single()
    }
}