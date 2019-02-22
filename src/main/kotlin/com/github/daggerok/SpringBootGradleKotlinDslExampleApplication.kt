package com.github.daggerok

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringBootGradleKotlinDslExampleApplication

fun main(args: Array<String>) {
  runApplication<SpringBootGradleKotlinDslExampleApplication>(*args)
}
