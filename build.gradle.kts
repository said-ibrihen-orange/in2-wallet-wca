import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	distribution
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	kotlin("plugin.jpa") version "1.7.22"
}

group = "es.in2"
version = "0.2.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations.implementation {
	exclude(group = "org.slf4j", module = "slf4j-simple")
}

repositories {
	mavenCentral()
	// Walt.id repositories. Doc: https://docs.walt.id/v/ssikit/getting-started/dependency-jvm
	maven("https://maven.walt.id/repository/waltid/")
	maven("https://maven.walt.id/repository/waltid-ssi-kit/")
	maven("https://repo.danubetech.com/repository/maven-public/")
	maven("https://jitpack.io")
}

dependencies {
	// Spring Boot Basic Configuration
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	// Works with @Json() or @Serializable
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
	// working with nonce
	implementation("com.nimbusds:nimbus-jose-jwt:9.30.1")
	implementation("com.google.guava:guava:31.1-jre")
	// https://github.com/cbeust/klaxon
	implementation("com.beust:klaxon:5.6")
	// SSI Kit Documentation: https://docs.walt.id/v/ssikit/getting-started/dependency-jvm
	// Walt.id dependencies
	// https://github.com/walt-id/waltid-ssikit
	implementation("id.walt:waltid-ssi-kit:1.13.0") //1.12.0
	// Walt.id VC library
	// https://github.com/walt-id/waltid-ssikit-vclib
	implementation("id.walt:waltid-ssikit-vclib:1.24.3") //1.24.0
	// Walt.id ServiceMatrix Library
	// https://github.com/walt-id/waltid-servicematrix
	implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.2") //1.1.0 //1.1.3
	// needed to work with the oidc
	// https://mvnrepository.com/artifact/com.nimbusds/oauth2-oidc-sdk
	//runtimeOnly("com.nimbusds:oauth2-oidc-sdk:10.7")
	implementation("com.nimbusds:oauth2-oidc-sdk:6.16.4")
	// Documentation
	implementation("org.springdoc:springdoc-openapi-starter-common:2.0.2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
	// Security
	//implementation("org.springframework.boot:spring-boot-starter-security")
	//testImplementation("org.springframework.security:spring-security-test")
	// Security - Auth (JWT)
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	implementation ("com.google.code.gson:gson:2.9.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
