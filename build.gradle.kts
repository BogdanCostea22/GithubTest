import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	id("com.google.cloud.tools.jib") version "3.3.1"

}

group = "ro.githubdemo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springdoc:springdoc-openapi-webflux-ui:1.6.12")
	implementation("org.springdoc:springdoc-openapi-kotlin:1.6.12")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test:3.5.0")
	testImplementation("org.mockito:mockito-core:4.8.1")
	testImplementation("org.mockito:mockito-inline:4.8.1")
	testImplementation("org.mockito:mockito-junit-jupiter:4.8.1")
//	testImplementation("app.cash.turbine:turbine:0.12.1")
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
	testImplementation("com.squareup.okhttp3:okhttp:4.10.0")
	testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs = mutableListOf("--enable-preview")
}

springBoot {
	mainClass.set("ro.githubdemo.demo.DemoApplicationKt")
}
