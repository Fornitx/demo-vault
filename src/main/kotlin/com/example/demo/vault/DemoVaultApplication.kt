package com.example.demo.vault

import com.example.demo.vault.vault.DemoVaultProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jooq.autoconfigure.JooqProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(JooqProperties::class, DemoVaultProperties::class)
@EnableScheduling
class DemoVaultApplication

fun main(args: Array<String>) {
	System.setProperty("org.jooq.no-logo", "true")
	System.setProperty("org.jooq.no-tips", "true")
	runApplication<DemoVaultApplication>(*args)
}
