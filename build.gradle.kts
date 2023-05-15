import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	distribution
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"
	id("jacoco")
	id("org.sonarqube") version "4.0.0.2929"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	kotlin("plugin.jpa") version "1.7.22"
}

group = "es.in2"
version = "2.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations.implementation {
	exclude(group = "org.slf4j", module = "slf4j-simple")
}

repositories {
	mavenLocal()
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://maven.walt.id/repository/waltid/")
	maven("https://maven.walt.id/repository/waltid-ssi-kit/")
	maven("https://repo.danubetech.com/repository/maven-public/")
}

dependencies {

	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	//implementation("org.springframework.boot:spring-boot-starter-oauth2-client:3.0.5")
	//implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.0.5")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Works with @Json() or @Serializable
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
	implementation("com.google.guava:guava:31.1-jre")
	implementation("com.beust:klaxon:5.6")

	// walt.id
	implementation("id.walt:waltid-ssikit:1.2304101159.0")
	implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.3")

	// nimbus
	implementation("com.nimbusds:nimbus-jose-jwt:9.30.2")
	//implementation("com.nimbusds:oauth2-oidc-sdk:10.7")

	// persistence
	testImplementation("com.h2database:h2:2.1.214")
	runtimeOnly("com.mysql:mysql-connector-j")

	// lombok
	compileOnly("org.projectlombok:lombok:1.18.26")
	annotationProcessor("org.projectlombok:lombok:1.18.26")
	testCompileOnly("org.projectlombok:lombok:1.18.26")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.26")

	// documentation
	implementation("org.springdoc:springdoc-openapi-starter-common:2.0.4")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")

	// security
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// testing
	testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.1")
	testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.8.1")
	testImplementation("org.mockito:mockito-core:3.12.4")
	testImplementation("io.mockk:mockk:1.13.5")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	//systemProperty("spring.profiles.active", "dev")
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		csv.required.set(false)
		html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
	}
}