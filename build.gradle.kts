import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.bundling.BootWar

plugins {
  war
  idea
  base
  java
  kotlin("jvm") version "1.3.21"
  kotlin("plugin.spring") version "1.3.21"
  id("io.spring.dependency-management") version "1.0.7.RELEASE"
  id("org.springframework.boot") version "2.2.0.BUILD-SNAPSHOT"
  id("com.avast.gradle.docker-compose").version("0.9.1")
  id("com.github.ben-manes.versions") version "0.21.0"
  id("com.moowork.node") version "1.2.0"
}

allprojects {
  val projectGroup: String by project
  val projectVersion: String by project
  group = projectGroup
  version = projectVersion
}

val kotlinVersion: String by project
val junitJupiterVersion: String by project
extra["kotlin.version"] = kotlinVersion
extra["junit-jupiter.version"] = junitJupiterVersion

tasks.withType<Wrapper>().configureEach {
  val gradleWrapperVersion: String by project
  gradleVersion = gradleWrapperVersion
  distributionType = Wrapper.DistributionType.BIN
}

val javaVersion = JavaVersion.VERSION_1_8

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "$javaVersion"
  }
}

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}

sourceSets {
  main {
    java.srcDir("src/main/kotlin")
  }
  test {
    java.srcDir("src/test/kotlin")
  }
}

/*
the<SourceSetContainer>()["main"].java.srcDir("src/main/kotlin")
the<SourceSetContainer>()["test"].java.srcDir("src/test/kotlin")
*/

repositories {
  mavenCentral()
  maven(url = "https://repo.spring.io/snapshot")
  maven(url = "https://repo.spring.io/milestone")
}

dependencyManagement {
  imports {
    val springBootVersion: String by project
    mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
  }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-hateoas")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  //implementation("org.springframework.boot:spring-boot-starter-web") // tomcat
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

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
  val targetNodeVersion: String by project
  val targetNpmVersion: String by project
  download = true
  version = targetNodeVersion
  npmVersion = targetNpmVersion
}

tasks.create("start")
tasks["start"].dependsOn("npm_start")
tasks["npm_start"].dependsOn("npm_i")
tasks["build"].dependsOn("npm_run_build")
tasks["npm_run_build"].dependsOn("npm_install")

val dockerPs: Task = tasks.create<Exec>("dockerPs") {
  shouldRunAfter("clean", "assemble")
  executable = "docker"
  args("ps", "-a", "-f", "name=${project.name}")
}

tasks["composeUp"].dependsOn("assemble")
tasks["composeUp"].shouldRunAfter("clean", "assemble")
dockerCompose {
  isRequiredBy(dockerPs)
}

// gradle dependencyUpdates -Drevision=release --parallel
tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
  resolutionStrategy {
    componentSelection {
      all {
        val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea", "SNAPSHOT")
            .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
            .any { it.matches(candidate.version) }
        if (rejected) reject("Release candidate")
      }
    }
  }
  //// optionals:
  // checkForGradleUpdate = true
  // outputFormatter = "plain" // "json" // "xml"
  // outputDir = "build/dependencyUpdates"
  // reportfileName = "report"
}

tasks {
  getByName("clean") {
    doLast {
      delete(
          project.buildDir,
          "${project.projectDir}/.vuepress/dist"
      )
    }
  }
}

tasks.create<Zip>("sources") {
  group = "Archive"
  description = "Archives sources in a zip archive"
  dependsOn("clean")
  shouldRunAfter("clean")
  from(".vuepress") {
    into(".vuepress")
  }
  from("src") {
    into("src")
  }
  from(
      ".gitignore",
      ".travis.yml",
      "build.gradle.kts",
      "docker-compose.yaml",
      "gradle.properties",
      "LICENSE",
      "package.json",
      "package-lock.json",
      "README.md",
      "settings.gradle.kts"
  )
  archiveFileName.set("${project.buildDir}/sources-${project.version}.zip")
}
