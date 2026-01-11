plugins {
    id("java")
}

group = "dev.kofeychi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("it.unimi.dsi:fastutil:8.5.12")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.guava:guava:32.1.2-jre")


    implementation("org.slf4j:slf4j-simple:1.6.1")
    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-core:2.25.3")
    implementation("org.slf4j:slf4j-log4j12:2.0.8")

    implementation("org.projectlombok:lombok:+")
    annotationProcessor("org.projectlombok:lombok:+")
    implementation("org.joml:joml:1.10.8")
    implementation("com.github.kwhat:jnativehook:+")
}

tasks.test {
    useJUnitPlatform()
}