package com.example.demo.vault

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class DemoDao(private val dslContext: DSLContext) {
    private val log = KotlinLogging.logger {}

    @EventListener(ApplicationReadyEvent::class)
    private fun appReadyEvent() {
        getFoo2()
    }

    fun getFoo(): String {
        val result = dslContext.fetch("select version()")
        val strings = result.into(String::class.java)
        log.info { "strings: $strings" }
        return strings.toString()
    }

    fun getFoo2(): String = runBlocking {
        val record = Flux.from(dslContext.resultQuery("select version()"))
            .awaitSingle()
        val string = record.into(String::class.java)
        log.info { "string: $string" }
        string
    }
}