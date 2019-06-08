import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    application
}
sourceSets {
    getByName("main").resources.srcDirs("src/main/resources")
}

sourceSets["main"].resources.srcDir("src/main/resources")
application {
    mainClassName = "moe.kageru.kagebot.MainKt"
}

group = "moe.kageru.kagebot"
version = "0.1"

repositories {
    mavenCentral()
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

dependencies {
    compile("com.moandjiezana.toml:toml4j:0.7.2")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.javacord:javacord:3.0.4")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("io.mockk:mockk:1.9")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}