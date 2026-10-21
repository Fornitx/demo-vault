package com.example.demo.vault.services

import com.example.demo.vault.db.DemoDao
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

@Service
class DemoService(private val demoDao: DemoDao) {

    fun getFoo(): Mono<String> {
        return demoDao.getFooR2dbc().doOnNext {
            log.info { "getFooR2dbc: $it" }
        }
    }
}
