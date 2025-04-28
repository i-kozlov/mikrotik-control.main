package com.baybaka.mikrotikcontrol

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MikrotikControlApplication

fun main(args: Array<String>) {
    runApplication<MikrotikControlApplication>(*args)
}
