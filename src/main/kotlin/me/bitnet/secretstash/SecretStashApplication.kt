package me.bitnet.secretstash

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SecretStashApplication

fun main(args: Array<String>) {
    runApplication<SecretStashApplication>(*args)
}
