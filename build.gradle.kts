import kr.entree.spigradle.kotlin.bungeecord

plugins {
    id("kr.entree.spigradle") version "2.2.4"
    java
}

group = "moe.nea89"
version = "0.0.1"

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.generateSpigotDescription.get().enabled = false

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(bungeecord("1.8"))
}