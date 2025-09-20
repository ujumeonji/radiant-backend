plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib")
}

dependencies {
    implementation(project(":radiant-core"))
    implementation(project(":radiant-eventstore"))
    implementation(project(":radiant-command"))
    implementation(project(":radiant-query"))
    implementation(project(":radiant-infrastructure"))
    implementation(project(":radiant-web"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    developmentOnly("org.springframework.ai:spring-ai-spring-boot-docker-compose")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter-test")
    testImplementation("com.netflix.graphql.dgs:dgs-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier.set("")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
    }
    to {
        image = "${System.getenv("ECR_REGISTRY") ?: "123456789012.dkr.ecr.ap-northeast-2.amazonaws.com"}/radiant-app"
        tags = setOf("latest", version.toString())
        credHelper {
            helper = "ecr-login"
        }
    }
    container {
        jvmFlags =
            listOf(
                "-Xms512m",
                "-Xmx2048m",
                "-XX:+UseG1GC",
                "-XX:MaxGCPauseMillis=100",
                "-XX:+UseStringDeduplication",
            )
        ports = listOf("8080")
        environment =
            mapOf(
                "SPRING_PROFILES_ACTIVE" to "prod",
                "JAVA_TOOL_OPTIONS" to "-XX:+ExitOnOutOfMemoryError",
            )
        labels =
            mapOf(
                "maintainer" to "radiant-team",
                "version" to version.toString(),
                "description" to "Radiant Application",
            )
        creationTime = "USE_CURRENT_TIMESTAMP"
        user = "1000:1000"
    }
    extraDirectories {
        paths {
            path {
                setFrom(file("src/main/jib"))
                into = "/app/config"
            }
        }
    }
}
