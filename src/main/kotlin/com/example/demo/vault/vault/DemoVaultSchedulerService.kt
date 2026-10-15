package com.example.demo.vault.vault

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DemoVaultSchedulerService(
    private val demoVaultClient: DemoVaultClient,
    private val credentialsProvider: CredentialsProvider,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(fixedDelay = 5_000)
    fun pollVault() {
//        val dataRaw = demoVaultClient.getDataRaw()
//        log.info { "DemoVaultClient dataRaw: $dataRaw" }
        val data = demoVaultClient.getData()
        log.info { "DemoVaultClient data: $data" }

        credentialsProvider.setCredentials(data.data)
    }
}