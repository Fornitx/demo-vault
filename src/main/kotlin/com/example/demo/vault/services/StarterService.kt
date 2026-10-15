package com.example.demo.vault.services

import com.example.demo.vault.vault.Credentials
import com.example.demo.vault.vault.CredentialsProvider
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.concurrent.thread

@Service
class StarterService(private val credentialsProvider: CredentialsProvider) {
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        thread(start = true) {
            Thread.sleep(10_000)
            credentialsProvider.setCredentials(Credentials("error", "error"))
        }
    }
}