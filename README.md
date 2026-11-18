# IT Helpdesk — Kotlin console

Première étape du projet POO : employés, techniciens et équipements.
Les données restent en mémoire. Aucun accès à une base de données, aucune interface graphique et aucun module Android.

## Démarrer dans IntelliJ IDEA

1. Ouvrir ce dossier avec **File > Open** et importer le projet Gradle.
2. Choisir un JDK **21** pour le projet et pour la JVM Gradle.
3. Utiliser le **Gradle Wrapper** fourni, puis attendre la synchronisation.
4. Exécuter `main()` dans `src/main/kotlin/projet01_helpdesk/app/Main.kt`, ou la tâche Gradle `application > run`.

## Démarrer dans PowerShell

```powershell
Set-Location 'C:\IRISI\S3\Programmation Mobile\Projets\IT Helpdesk - Kotlin'
.\gradlew.bat run
```

Pour compiler, produire la distribution et vérifier les règles :

```powershell
.\gradlew.bat build
```

Pour relancer seulement les vérifications :

```powershell
.\gradlew.bat verifierEtape1
```

Java 21 doit être accessible via `JAVA_HOME` ou le `PATH`. Le premier lancement télécharge Gradle et les dépendances Kotlin ; une connexion Internet est donc nécessaire. Aucune installation globale de Gradle ou de Kotlin n'est requise.

## Organisation

```text
src/main/kotlin/projet01_helpdesk/
├── personnes/
│   ├── Employe.kt
│   └── TechnicienSupport.kt
├── materiel/
│   └── Equipement.kt
├── incidents/       # Étapes 2 et 3, à développer
├── notifications/   # Étape 4, à développer
├── services/        # Étape 4, à développer
└── app/
    └── Main.kt
```

Le préfixe `src/main/kotlin` suit la convention Gradle ; les noms des packages respectent le cahier des charges de la conversation « Apprendre Kotlin Débutant ».

## Règles de l'étape 1

- `Employe` est une classe `open`. Son matricule et son nom sont obligatoires ; `afficherInformations()` et `travailler()` sont redéfinissables.
- `TechnicienSupport` hérite d'`Employe`, ajoute une spécialité obligatoire, redéfinit les deux méthodes et propose `diagnostiquer(description)`.
- Les informations descriptives sont des `val` : elles ne peuvent pas être remplacées après construction.
- Chaque équipement commence opérationnel, avec zéro panne. `enPanne` et `nombrePannes` restent consultables, mais leurs setters sont privés.
- `declarerPanne()` augmente le compteur seulement lors du passage d'opérationnel à en panne.
- `reparer()` remet l'équipement en service sans effacer son historique de pannes.
- Déclarer une panne déjà déclarée, ou réparer un équipement opérationnel, lève `IllegalStateException` sans modifier l'état ni le compteur.
- Les chaînes vides ou composées uniquement d'espaces sont refusées avec `require()` (`IllegalArgumentException`). Le type reste une chaîne libre non vide, comme prévu pour cette première étape.

`Main.kt` sert de démonstration et de vérification exécutable : polymorphisme, PC/imprimante/serveur, deux cycles de panne, opérations invalides et validation de chaque champ. Les lignes « Refus attendu » indiquent des cas négatifs correctement détectés. Une assertion qui échoue interrompt le programme et fait échouer la tâche `check`/`build`. Il n'y a pas encore de suite JUnit distincte.

## Configuration

- Kotlin/JVM 2.2.10
- Gradle Wrapper 8.14.3, avec vérification SHA-256 de la distribution
- JDK et cible JVM 21
- Point d'entrée : `projet01_helpdesk.app.MainKt`

Références officielles : [compatibilité Kotlin/Gradle](https://kotlinlang.org/docs/gradle-configure-project.html) et [Gradle 8.14.3](https://docs.gradle.org/8.14.3/release-notes.html).

## Suite prévue

2. `Incident` abstrait, `IncidentMateriel` et `IncidentLogiciel`.
3. `Ticket`, `Intervention` et transitions Ouvert → En cours → Résolu → Clôturé.
4. `ServiceSupport`, contrôle des doublons et notifications Email/SMS simulées.
5. Intégration et tests du parcours complet.

Ces modules sont réservés dans l'arborescence, mais ne sont pas encore implémentés.

## Vérification effectuée

Le 18 novembre 2026, la commande `./gradlew.bat --no-daemon --console=plain build run` s'est terminée avec `BUILD SUCCESSFUL` sous Java 21.0.9. Les vérifications de l'étape 1 sont exécutées par `verifierEtape1`, rattachée à `check`, et ont toutes réussi.

Le projet utilise l'adresse officielle alternative `https://repo1.maven.org/maven2` de Maven Central, car la résolution réseau de `repo.maven.apache.org` échouait sur cette machine lors de la préparation.
