package com.example.demo.vault.db

import org.jooq.DSLContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

private const val versionQuery = "select version()"
private const val idQuery = "select system_identifier from pg_control_system()"

@Component
class DemoDao(private val dslContext: DSLContext) {
//    fun getFooJdbc(): String {
//        val result = dslContext.fetch(idQuery)
//        val strings = result.into(String::class.java)
//        return strings.toString()
//    }

    fun getFooR2dbc(): Mono<String> {
        return Mono.from(dslContext.resultQuery(idQuery))
            .map { it.into(String::class.java) }
    }
}