plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    alias(libs.plugins.mockito)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq") {
        exclude(group = "io.r2dbc")
    }
	implementation("org.springframework.boot:spring-boot-starter-quartz")
//	implementation("org.springframework.boot:spring-boot-starter-r2dbc")
//	implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
//	implementation("org.springframework.boot:spring-boot-starter-webclient")
//	implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.vault:spring-vault-core")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation(libs.kotlin.logging)
//	implementation(libs.scoped.mdc)

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
//	runtimeOnly("org.postgresql:r2dbc-postgresql")
//	runtimeOnly("io.r2dbc:r2dbc-pool")
    runtimeOnly("io.r2dbc:r2dbc-spi")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test") {
        exclude(group = "io.r2dbc")
    }
	testImplementation("org.springframework.boot:spring-boot-starter-quartz-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-r2dbc-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.bom.get().toString())
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<DependencyReportTask>("allDeps")