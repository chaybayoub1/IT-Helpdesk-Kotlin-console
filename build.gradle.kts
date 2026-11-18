plugins {
    kotlin("jvm") version "2.2.10"
    application
}

repositories {
    maven { url = uri("https://repo1.maven.org/maven2") }
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("projet01_helpdesk.app.MainKt")
}

// Le programme de démonstration vérifie aussi les règles de l'étape 1.
val verifierEtape1 by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Vérifie les validations et le cycle des pannes."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(application.mainClass)
    dependsOn(tasks.classes)
}

tasks.check {
    dependsOn(verifierEtape1)
}
