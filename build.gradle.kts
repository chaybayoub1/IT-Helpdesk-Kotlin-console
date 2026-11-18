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

val verifierEtape1 by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Vérifie les validations et le cycle des pannes."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("projet01_helpdesk.verification.etape1.VerificationEtape1Kt")
    dependsOn(tasks.testClasses)
}

val verifierProjet by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Exécute les scénarios de vérification du projet."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("projet01_helpdesk.verification.VerificationProjetKt")
    dependsOn(tasks.testClasses)
}

tasks.check {
    dependsOn(verifierProjet)
}
