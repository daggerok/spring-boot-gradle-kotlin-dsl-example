# spring-boot + gradle-kotlin-dsl

## Table of Content

<!--ts-->
  * [Spring Boot](#Spring-Boot)
    * [plugins definition](#plugins-definition)
    * [dependencies](#dependencies)
    * [executable artifact](#executable-artifact)
    * [kotlin](#kotlin)
    * [override parent versions](#override-parent-versions)
    * [war](#war)
  * [JUnit / Jupiter (GitHub)](#JUnit--Jupiter) [JUnit / Jupiter (VuePress)](#JUnit-Jupiter)
  * [java <-> kotlin (GitHub)](#java---kotlin) [java <-> kotlin (VuePress)](#java-kotlin)
  * [Wrapper](#Wrapper)
  * [NodeJS](#NodeJS)
  * [docker](#docker)
  * [create sources.zip](#create-sources-zip)
  * [links and resources](#links-and-resources)
<!--ts-->

- Travis CI status: [![Build Status](https://travis-ci.org/daggerok/spring-boot-gradle-kotlin-dsl-example.svg?branch=master)](https://travis-ci.org/daggerok/spring-boot-gradle-kotlin-dsl-example)
- Documentation on [GitHub Pages](https://daggerok.github.io/spring-boot-gradle-kotlin-dsl-example/)
- GitHub [daggerok/spring-boot-gradle-kotlin-dsl-example](https://github.com/daggerok/spring-boot-gradle-kotlin-dsl-example) repository 

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

::: tip
we need this because we wanna use spring 2.2.0-BUILD-SNAPSHOT version
:::

### dependencies

_build.gradle.kts_

```kotlin
dependencies {
  implementation("org.springframework.boot:spring-boot-starter-hateoas")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter")
  annotationProcessor("org.projectlombok:lombok")
  testAnnotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  runtimeOnly("org.springframework.boot:spring-boot-devtools")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
}
```

### executable artifact

_build.gradle.kts_

```kotlin
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>().configureEach {
  launchScript()
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

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
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

_build and run executable WAR artifact_

```bash
./gradlew build
bash ./build/libs/*.war
```

## JUnit / Jupiter

_build.gradle.kts_

```kotlin
dependencies {
  testImplementation("junit:junit")
  testAnnotationProcessor("org.projectlombok:lombok")
  testImplementation(platform("org.junit:junit-bom:$junitJupiterVersion"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  testRuntime("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    showExceptions = true
    showStandardStreams = true
    events(PASSED, SKIPPED, FAILED)
  }
}
```

## java <-> kotlin

_don't miss your `src/*/kotlin/**.java` and `src/*/java/**.kt` sources files location!_

```kotlin
sourceSets {
  main {
    java.srcDir("src/main/kotlin")
  }
  test {
    java.srcDir("src/test/kotlin")
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
  npmVersion = "6.9.0"
}

tasks.create("start")
tasks["start"].dependsOn("npm_start")
tasks["npm_start"].dependsOn("npm_i")
tasks["build"].dependsOn("npm_run_build")
tasks["npm_run_build"].dependsOn("npm_install")
```

_run 'npm start' by using gradle node plugin_

```bash
./gradlew start
```

_build VuePress documentation_

```bash
./gradlew npm_run_build
```

## docker

_docker-compose.yaml_

```yaml
version: '3.7'
services:
  busybox:
    image: busybox
    command: "echo Hello"
    healthcheck:
      disable: true
networks:
  hello-net:
    driver: bridge
```

_build.gradle.kts_

```kotlin
plugins {
  id("com.avast.gradle.docker-compose").version("0.8.14")//.apply(false)
}

val dockerPs: Task = tasks.create<Exec>("dockerPs") {
  dependsOn("assemble")
  shouldRunAfter("assemble")
  executable = "docker"
  args("ps", "-a", "-f", "name=${project.name}")
}

apply(plugin = "com.avast.gradle.docker-compose")

dockerCompose {
  isRequiredBy(dockerPs)
}
```

_run and test_

```bash
./gradlew composeUp
http :8080/actuator
./gradlew composeDown
```

## create sources.zip

```kotlin

tasks {
  getByName("clean") {
    doLast {
      delete(project.buildDir)
    }
  }
}

tasks.create<Zip>("sources") {
  dependsOn("clean")
  shouldRunAfter("clean")
  description = "Archives sources in a zip file"
  group = "Archive"
  from("src") {
    into("src")
  }
  from("build.gradle.kts")
  from("settings.gradle.kts")
  from(".vuepress") {
    into(".vuepress")
  }
  from("README.md")
  from("package.json")
  archiveFileName.set("${project.buildDir}/sources-${project.version}.zip")
}
```

## links and resources

- [from groovy to kotlin DSL gradle migration guide (nice but little bit old)](https://github.com/jnizet/gradle-kotlin-dsl-migration-guide)
- [bmuschko docker plugins](https://bmuschko.github.io/gradle-docker-plugin/)
- [gradle docker-compose plugin](https://github.com/avast/gradle-docker-compose-plugin)
