package com.example.demo.vault.vault.providers

import com.example.demo.vault.vault.data.Credentials
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

private const val THREAD_NAME = "demo-vault-daemon-thread"

private val log = KotlinLogging.logger {}

class CachedCredentialsProvider(
    private val delegate: CredentialsProvider,
) : UpdatableCredentialsProvider, ListenableCredentialsProvider {
    private val credentialsRef = AtomicReference<Credentials>()

    private val credentialsListeners = ArrayList<CredentialsListener>()

    private val scheduledExecutorService: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, THREAD_NAME).apply { isDaemon = true }
        }
    private var future: ScheduledFuture<*>? = null

    init {
        setCredentials(delegate.getCredentials())
    }

    override fun getCredentials(): Credentials {
        return credentialsRef.get()
    }

    override fun addCredentialsListener(credentialsListener: CredentialsListener) {
        credentialsListeners += credentialsListener
    }

    override fun update() {
        log.debug { "Cached credentials update" }
        val currentCreds = credentialsRef.get()
        val credentials = delegate.getCredentials()
        if (currentCreds != credentials) {
            setCredentials(credentials)
        }
    }

    fun setCredentials(credentials: Credentials) {
        log.debug { "Setting credentials: $credentials" }

        if (Thread.currentThread().name != THREAD_NAME) {
            log.debug { "Cancel future from thread ${Thread.currentThread().name}" }
            future?.cancel(true)
        }

        credentialsRef.set(credentials)

        for (listener in credentialsListeners) {
            listener.credentialsChanged(credentials)
        }

        val delay = max(10, credentials.ttl)
        log.debug { "Credentials update scheduled for $delay seconds" }
        future = scheduledExecutorService.schedule(::update, delay, TimeUnit.SECONDS)
    }
}
