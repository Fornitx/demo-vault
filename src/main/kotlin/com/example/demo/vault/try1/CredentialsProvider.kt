package com.example.demo.vault.try1

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

@Component
class CredentialsProvider {
    private val log = KotlinLogging.logger {}

    private val credentials = AtomicReference<Credentials>(null)

    fun getCredentials(): Credentials {
        return credentials.get()
    }

    fun setCredentials(username: String?, password: String?) {
        log.info { "Setting credentials with username $username and password $password" }
        credentials.set(Credentials(username, password))
    }

    data class Credentials(val username: String?, val password: String?)
}