package com.example.demo.vault.vault.test

import com.example.demo.vault.db.DemoDao
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
@Deprecated("")
class DemoVaultSchedulerService(private val demoDao: DemoDao) {
    @Scheduled(fixedDelay = 5_000)
    fun pollDb() {
        demoDao.getFooRaw().doOnNext {
            log.info { "DemoDao getFooRaw(): $it" }
        }.block()
    }
}