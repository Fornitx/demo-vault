package com.example.demo.vault.services

import com.example.demo.vault.db.DemoDao
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class DemoService(private val demoDao: DemoDao) {

    fun getFoo(): String {
        val string = demoDao.getFooJdbc()
        log.info { "getFooJdbc: $string" }
        return string
    }
}
