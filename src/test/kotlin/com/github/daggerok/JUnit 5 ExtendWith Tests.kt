package com.github.daggerok

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT,
    classes = [SptingBootGradleKotlinDslExampleApplication::class])
class `JUnit 5 ExtendWith Tests` {

  @Test fun `also testing context`() {}
}
