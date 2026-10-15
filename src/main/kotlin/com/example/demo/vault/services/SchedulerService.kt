package com.example.demo.vault.services

import com.example.demo.vault.db.DemoDao
import com.example.demo.vault.rest.clients.DemoVaultClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulerService(
    private val demoDao: DemoDao,
    private val demoVaultClient: DemoVaultClient,
    private val env: Environment,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(fixedDelay = 5_000)
    fun pollDb() {
        demoDao.getFooR2dbc().doOnNext {
            log.info { "DemoDao getFooR2dbc: $it" }
        }.block()
    }

    @Scheduled(fixedDelay = 5_000)
    fun pollVault() {
        demoVaultClient.getData().doOnNext {
            log.info { "DemoVaultClient data: $it" }
        }.block()
    }

    @Scheduled(fixedDelay = 5_000)
    fun pollEnv() {
        log.info { "Environment password: ${env.getProperty("password")}" }
    }
}