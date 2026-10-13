package com.example.demo.vault

import org.springframework.stereotype.Service

@Service
class DemoService(private val demoDao: DemoDao) {
    fun getFoo(): String {
        return demoDao.getFoo()
    }
}
