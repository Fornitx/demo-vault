package com.example.demo.vault.db

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DemoDbSchedulerService(private val demoDao: DemoDao) {
    private val log = KotlinLogging.logger {}

    @Scheduled(fixedDelay = 5_000)
    fun pollDb() {
        demoDao.getFooR2dbc().doOnNext {
            log.info { "DemoDao getFooR2dbc: $it" }
        }.block()
    }
}