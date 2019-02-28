import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.bundling.BootWar

plugins {
  id("war")
  id("idea")
  id("com.moowork.node") version "1.2.0"
  id("org.jetbrains.kotlin.jvm") version "1.3.21"
  id("org.jetbrains.kotlin.plugin.spring") version "1.3.21"
  id("org.springframework.boot") version "2.2.0.BUILD-SNAPSHOT"
  id("com.bmuschko.docker-remote-api").version("4.5.0").apply(false)
  id("com.avast.gradle.docker-compose").version("0.8.14")//.apply(false)
}

group = "com.github.daggerok"
version = "1.0.0-SNAPSHOT"

val gradleVersion = "5.2.1"
val kotlinVersion = "1.3.21"
val junitJupiterVersion = "5.4.0"
val javaVersion = JavaVersion.VERSION_1_8

extra["kotlin.version"] = kotlinVersion
extra["junit-jupiter.version"] = junitJupiterVersion

apply(plugin = "io.spring.dependency-management")

tasks.withType<Wrapper>().configureEach {
  gradleVersion = gradleVersion
  distributionType = Wrapper.DistributionType.BIN
}

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}

/*
sourceSets {
  main {
    java.srcDir("src/main/kotlin")
  }
  test {
    java.srcDir("src/test/kotlin")
  }
}
*/
the<SourceSetContainer>()["main"].java.srcDir("src/main/kotlin")
the<SourceSetContainer>()["test"].java.srcDir("src/test/kotlin")

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "$javaVersion"
  }
}

repositories {
  mavenCentral()
  maven(url = "https://repo.spring.io/snapshot")
  maven(url = "https://repo.spring.io/milestone")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-hateoas")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  //implementation("org.springframework.boot:spring-boot-starter-web") // tomcat
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  annotationProcessor("org.projectlombok:lombok")
  testAnnotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")

  testImplementation("junit:junit")
  testImplementation(platform("org.junit:junit-bom:$junitJupiterVersion"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  testRuntime("org.junit.platform:junit-platform-launcher")
}

tasks.withType<BootJar>().configureEach {
  launchScript()
}

tasks.withType<BootWar>().configureEach {
  launchScript()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    showExceptions = true
    showStandardStreams = true
    events(PASSED, SKIPPED, FAILED)
  }
}

defaultTasks("build")

node {
  download = true
  version = "10.9.0"
  npmVersion = "6.8.0"
}

tasks.create("start")
tasks["start"].dependsOn("npm_start")
tasks["build"].dependsOn("npm_run_build")

val busybox: Task = tasks.create<Exec>("busybox") {
  executable = "docker"
  args("ps", "-a", "-f", "name=spring-boot-gradle-kotlin-dsl-example")
}

apply(plugin = "com.avast.gradle.docker-compose")

dockerCompose {
  isRequiredBy(busybox)
}
