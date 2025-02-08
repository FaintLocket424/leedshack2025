plugins {
    id("application")
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.opencsv:opencsv:5.9")
}

tasks.test {
    useJUnitPlatform()
}

project.setProperty("mainClassName", "org.example.Main")

//tasks.withType<Jar> {
//    manifest {
//        attributes["Main-Class"] = "org.example.Main"
//    }
//}