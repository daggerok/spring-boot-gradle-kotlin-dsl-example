# [spring-boot + gradle-kotlin-dsl](https://github.com/daggerok/spring-boot-gradle-kotlin-dsl-example) [![Build Status](https://travis-ci.org/daggerok/spring-boot-gradle-kotlin-dsl-example.svg?branch=master)](https://travis-ci.org/daggerok/spring-boot-gradle-kotlin-dsl-example)

## Spring Boot 

### plugins definition

_build.gradle.kts_

```kotlin
plugins {
  id("org.jetbrains.kotlin.jvm").version("1.3.21")
  id("org.jetbrains.kotlin.plugin.spring").version("1.3.21")
  id("org.springframework.boot").version("2.2.0.BUILD-SNAPSHOT")
}

apply(plugin = "war")
apply(plugin = "io.spring.dependency-management")

repositories {
  mavenCentral()
  maven(url = "https://repo.spring.io/snapshot")
  maven(url = "https://repo.spring.io/milestone")
}
```

_settings.gradle.kts_

```kotlin
pluginManagement {
  repositories {
    gradlePluginPortal()
    maven(url = "https://repo.spring.io/snapshot")
    maven(url = "https://repo.spring.io/milestone")
    gradlePluginPortal()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "org.springframework.boot") {
        useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
      }
    }
  }
}
```

### kotlin

_build.gradle.kts_

```kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = JavaVersion.VERSION_1_8.toString()
  }
}
```

### override parent versions

_build.gradle.kts_

```kotlin
val kotlinVersion = "1.3.21"
val junitJupiterVersion = "5.4.0"

extra["kotlin.version"] = kotlinVersion
extra["junit-jupiter.version"] = junitJupiterVersion
```

---
**NOTE**

we need this because we wanna use spring 2.2.0-BUILD-SNAPSHOT version

---

## war

_build.gradle.kts_

```kotlin
plugins {
  id("war")
}

tasks.withType<BootWar>().configureEach {
  launchScript()
}
```

_ServletInitializer.kt_

```kotlin
class ServletInitializer : SpringBootServletInitializer() {
  override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
    return application.sources(SptingBootGradleKotlinDslExampleApplication::class.java)
  }
}
```

## Wrapper

_build.gradle.kts_

```kotlin
tasks.withType<Wrapper>().configureEach {
  gradleVersion = gradleVersion
  distributionType = Wrapper.DistributionType.BIN
}
```

_re-generate gradle wrapper_

```bash
./gradlew :wrapper
```

## NodeJS

_build.gradle.kts_

```kotlin
plugins {
  id("com.moowork.node") version "1.2.0"
}

node {
  download = true
  version = "10.9.0"
  npmVersion = "6.8.0"
}

tasks.create("start")
tasks["start"].dependsOn("npm_start")
tasks["build"].dependsOn("npm_run_build")
```

## links and resources

- [from groovy to kotlin DSL gradle migration guide (nice but little bit old)](https://github.com/jnizet/gradle-kotlin-dsl-migration-guide)
