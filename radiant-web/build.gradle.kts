plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
    id("com.netflix.dgs.codegen")
}

dependencies {
    implementation(project(":radiant-core"))
    implementation(project(":radiant-command"))
    implementation(project(":radiant-query"))
    implementation(project(":radiant-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.test:spring-test")
    testImplementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.generateJava {
    schemaPaths.add("$projectDir/src/main/resources/graphql-client")
    packageName = "ink.radiant.web.codegen"
    generateClient = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
