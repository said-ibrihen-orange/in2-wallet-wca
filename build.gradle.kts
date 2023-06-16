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
version = "1.5.0"
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
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")

	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.testng:testng:7.7.0")

    // Persistence Layer
	runtimeOnly("com.h2database:h2:2.1.214")
	runtimeOnly("com.mysql:mysql-connector-j")

	// lombok
	compileOnly("org.projectlombok:lombok:1.18.26")
	annotationProcessor("org.projectlombok:lombok:1.18.26")

	// OpenAPI
	//implementation("org.springdoc:springdoc-openapi-starter-common:2.0.4")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")

	// walt.id
	implementation("id.walt:waltid-ssikit:1.2305121558.0")
	implementation("id.walt.servicematrix:WaltID-ServiceMatrix:1.1.3")

	// nimbus-jjwt
	implementation("com.nimbusds:nimbus-jose-jwt:9.30.2")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// json
	implementation("org.json:json:20230227")
	implementation("com.googlecode.json-simple:json-simple:1.1.1")
	implementation("com.google.code.gson:gson:2.10.1")

	// testing
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-aop")
	testImplementation("com.h2database:h2:2.1.214")
	testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.1")
	testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.8.1")
	testImplementation("org.mockito:mockito-core:3.12.4")
	testImplementation("io.mockk:mockk:1.13.5")
	testCompileOnly("org.projectlombok:lombok:1.18.26")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.26")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

/*
val testCoverage by tasks.registering {
	group = "verification"
	description = "Runs the unit tests with coverage."
	dependsOn(":test", ":jacocoTestReport", ":jacocoTestCoverageVerification")
	val jacocoTestReport = tasks.findByName("jacocoTestReport")
	jacocoTestReport?.mustRunAfter(tasks.findByName("test"))
	tasks.findByName("jacocoTestCoverageVerification")?.mustRunAfter(jacocoTestReport)
}
*/

tasks.withType<Test> {
	useJUnitPlatform()
	tasks.jacocoTestReport
	finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		csv.required.set(false)
		html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
	}
	classDirectories.setFrom(
		sourceSets.main.get().output.asFileTree.matching {
			//exclude(
				//"es/in2/wallet/*.*",
				//"es/in2/wallet/configuration/**",
				//"es/in2/wallet/controller/**",
				//"es/in2/wallet/exception/**",
				//"es/in2/wallet/model/**",
				//"es/in2/wallet/security/**",
				//"es/in2/wallet/service/**",
				//"es/in2/wallet/util/**",
				//"es/in2/wallet/waltid/impl/**",
			//)
		}
	)
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)
	violationRules {
		rule {
			isEnabled = true
			element = "PACKAGE"
			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.1".toBigDecimal()
				maximum = "1.0".toBigDecimal()
			}
		}
		classDirectories.setFrom(
			sourceSets.main.get().output.asFileTree.matching {
				exclude(
					"es/in2/wallet/WalletBackendApplication.kt",
					"es/in2/wallet/model/*",
					"es/in2/wallet/util",
					"es/in2/wallet/waltid"
				)
			}
		)
	}
}

