package com.example.demo.vault.services

import com.example.demo.vault.try1.CredentialsProvider
import org.springframework.stereotype.Service
import kotlin.concurrent.thread

@Service
class StarterService(private val credentialsProvider: CredentialsProvider) {
//    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        thread(start = true) {
            Thread.sleep(5_000)
            credentialsProvider.setCredentials("pguser", "pgpass123")
        }
    }
}