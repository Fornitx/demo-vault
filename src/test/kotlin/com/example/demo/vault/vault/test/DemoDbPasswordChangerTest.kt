package com.example.demo.vault.vault.test

import org.junit.jupiter.api.Test

class DemoDbPasswordChangerTest {
    @Test
    fun test() {
        DemoDbPasswordChanger.changePassword(Constants.NEW_PASSWORD, Constants.PASSWORD)
//        DemoDbPasswordChanger.changePassword(Constants.PASSWORD, Constants.NEW_PASSWORD)
    }
}