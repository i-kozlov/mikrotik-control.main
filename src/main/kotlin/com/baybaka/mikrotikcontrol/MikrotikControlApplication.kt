package com.baybaka.mikrotikcontrol

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class MikrotikControlApplication

fun main(args: Array<String>) {
    runApplication<MikrotikControlApplication>(*args)
}
