package com.example.demo.vault.controllers

import com.example.demo.vault.services.DemoService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class DemoController(private val service: DemoService) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/foo")
    fun getFoo(): Mono<String> {
        log.info { "GET /foo" }
        return service.getFoo()
    }
}