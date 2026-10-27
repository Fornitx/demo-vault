package com.example.demo.vault.vault.test

import com.example.demo.vault.vault.data.Credentials
import com.example.demo.vault.vault.providers.CachedCredentialsProvider
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Service
@Deprecated("")
class DemoVaultStarterService(private val credentialsProvider: CachedCredentialsProvider) {
    private val methodHandle: MethodHandle = MethodHandles.privateLookupIn(
        CachedCredentialsProvider::class.java, MethodHandles.lookup()
    ).findVirtual(
        CachedCredentialsProvider::class.java,
        "setCredentials",
        MethodType.methodType(Void.TYPE, Credentials::class.java)
    )

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        thread(start = true) {
            TimeUnit.SECONDS.sleep(10)

            DemoDbPasswordChanger.changePassword(Constants.PASSWORD, Constants.NEW_PASSWORD)
            setPassword(Constants.NEW_PASSWORD)

            TimeUnit.SECONDS.sleep(30)

            DemoDbPasswordChanger.changePassword(Constants.NEW_PASSWORD, Constants.PASSWORD)
            setPassword(Constants.PASSWORD)
        }
    }

    private fun setPassword(password: String) {
        val currentCredentials = credentialsProvider.getCredentials()
        methodHandle.invoke(credentialsProvider, currentCredentials.copy(password = password))
    }
}