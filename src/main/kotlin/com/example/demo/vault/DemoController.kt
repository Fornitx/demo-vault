package com.example.demo.vault

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoController(private val service: DemoService) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/foo")
    fun getFoo(): String {
        log.info { "GET /foo" }
        return service.getFoo()
    }
}