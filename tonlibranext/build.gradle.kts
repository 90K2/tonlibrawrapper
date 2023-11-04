import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


group = "com.tonlibwrapper"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven("https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/")
}

configurations.all {
    exclude(group = "org.ton", module = "ton-kotlin-cell-jvm")
}

dependencies {
	api("org.ton:ton-kotlin-jvm:0.3.0-20230412.120307-1")
	api(files("./libs/ton-kotlin-cell-jvm-0.3.0-SNAPSHOT.jar"))

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.6")
	implementation("org.springframework.boot:spring-boot-starter-webflux:2.7.0")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	testImplementation(kotlin("test"))
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
