# IT Helpdesk — Kotlin console

Application de gestion des incidents informatiques, réalisée en cinq étapes.
Employés, techniciens, équipements, incidents, tickets, interventions et notifications sont intégrés.
Les données restent **en mémoire pendant la session**. Les notifications sont simulées par affichage ; aucun Email ou SMS réel n'est envoyé.

## Lancer dans IntelliJ IDEA

1. Ouvrir ce dossier avec **File > Open**, puis importer le projet Gradle.
2. Sélectionner le **JDK 21** pour le projet et la JVM Gradle ; utiliser le Wrapper fourni.
3. Exécuter `main()` dans `src/main/kotlin/projet01_helpdesk/app/Main.kt`.
4. Le menu démarre avec un service vide. Pour voir un exemple complet, ajouter `--demo` aux arguments du programme.

## Lancer dans PowerShell

```powershell
Set-Location 'C:\IRISI\S3\Programmation Mobile\Projets\IT Helpdesk - Kotlin'

# Menu interactif
.\gradlew.bat --console=plain run

# Démonstration complète : incidents matériel et logiciel
.\gradlew.bat --console=plain run --args="--demo"

# Même démonstration avec les notifications SMS
.\gradlew.bat --console=plain run --args="--demo --sms"

# Compiler, vérifier et générer les distributions
.\gradlew.bat build

# Relancer tous les scénarios de vérification
.\gradlew.bat verifierProjet

# Relancer uniquement les vérifications d'origine
.\gradlew.bat verifierEtape1
```

Le premier lancement télécharge Gradle et Kotlin. Une fois les dépendances présentes, les commandes fonctionnent avec `--offline`.
Java 21 doit être accessible via `JAVA_HOME` ou le `PATH`.
Aucune installation globale de Kotlin ou de Gradle n'est requise.

Pour démarrer ensuite sans passer par Gradle :

```powershell
.\gradlew.bat installDist
& '.\build\install\it-helpdesk-kotlin\bin\it-helpdesk-kotlin.bat'
```

La distribution ZIP est produite dans `build/distributions/it-helpdesk-kotlin.zip` et nécessite également Java 21.

## Parcours dans le menu

1. Enregistrer un employé (choix **1**), un technicien (**2**) et un équipement (**3**).
2. Pour une panne matérielle, déclarer la panne (**5**), puis ouvrir le ticket (**7**).
3. Affecter le technicien (**8**) et enregistrer son intervention (**9**).
4. Après réparation réelle du matériel, remettre l'équipement en service (**6**).
5. Confirmer que le problème est effectivement traité (**10**, réponse `oui`), résoudre (**11**), puis clôturer (**12**).
6. Consulter les tickets (**13**) ou rechercher un numéro et son historique (**14**).
7. Quitter avec **0**. Les données de cette session ne sont pas sauvegardées.

Les incidents logiciels suivent le même parcours, sans imposer de panne ni de réparation matérielle.
Le choix **4** affiche l'annuaire et le parc ; **15** affiche les éventuels échecs de notification.
Une saisie invalide affiche une erreur puis revient au menu. Une fin d'entrée termine proprement la session.

## Structure

```text
src/main/kotlin/projet01_helpdesk/
├── personnes/       Employe, TechnicienSupport
├── materiel/        Equipement
├── incidents/       Incident, IncidentMateriel, IncidentLogiciel,
│                    Ticket, Intervention
├── notifications/   Notificateur, NotificationEmail, NotificationSMS
├── services/        ServiceSupport
└── app/             Main, ConsoleHelpdesk, Demonstration

src/test/kotlin/projet01_helpdesk/verification/
├── etape1/VerificationEtape1.kt
├── VerificationProjet.kt
├── VerificationTickets.kt
├── VerificationService.kt
└── VerificationIntegration.kt
```

## Règles métier

- Les informations obligatoires sont validées avec `require()`. Les propriétés descriptives sont en lecture seule.
- L'état de panne et son compteur possèdent des setters privés. Une nouvelle panne augmente le compteur une seule fois ; une réparation conserve le total.
- `Incident` est abstrait. Les incidents matériels et logiciels implémentent leurs diagnostics propres.
- Les statuts restent des chaînes, regroupées en constantes : **Ouvert → En cours → Résolu → Clôturé**.
- Un ticket ouvert peut recevoir un technicien une seule fois. Les interventions ne sont autorisées qu'en cours.
- Chaque intervention conserve sa description, son technicien et sa date. Les changements importants alimentent l'historique.
- La résolution exige au moins une intervention et une confirmation explicite du traitement. Pour un incident matériel, l'équipement doit être opérationnel.
- Toute nouvelle intervention annule la confirmation précédente : le technicien doit confirmer de nouveau le résultat.
- Un incident logiciel peut être résolu indépendamment d'une panne matérielle distincte du même équipement.
- Seul un ticket résolu peut être clôturé. Les opérations invalides ne changent ni le statut ni l'historique.
- Les matricules, références et numéros sont uniques dans le service, sans distinction de casse ni d'espaces extérieurs.
- Un ticket enregistré utilise les instances d'employé et d'équipement du service. L'affectation par le service exige un technicien enregistré.
- Les collections sont privées ; les listes retournées sont des copies. Les objets métier restent consultables et proposent leurs opérations contrôlées.
- Le service envoie une notification après chaque opération réussie sur un ticket. Un échec du canal est signalé et mémorisé sans annuler l'opération métier.
- Pour bénéficier de ces notifications et contrôles d'enregistrement, l'application passe par `ServiceSupport`.

## Vérifications et résultat

Chaque étape a été compilée et vérifiée avant son commit.
Le 18 novembre 2026, `build run --args=--demo installDist` a réussi sous Java 21 :
**22 scénarios**, plus les vérifications initiales de l'étape 1.

Les tests couvrent les transitions autorisées/interdites, les champs invalides, les doublons,
les références inconnues, les copies des collections, les notifications, les deux parcours complets,
le menu et la fin de saisie. Les assertions font échouer Gradle si une règle n'est pas respectée.

La suite utilise des programmes Kotlin de vérification sans dépendance JUnit :
`verifierProjet` est rattaché à `check` et `build`. La tâche `test` déclenche aussi
`verifierProjet`, mais son moteur JUnit est désactivé (ligne `test SKIPPED` attendue).

## Étapes et Git

| Étape | Contenu | État |
|---|---|---|
| 1 | Employés, techniciens, équipements et validations | Terminée, commit initial |
| 2 | Abstraction et incidents matériels/logiciels | Terminée, commit dédié |
| 3 | Tickets, interventions et cycle de vie | Terminée, commit dédié |
| 4 | Service support, recherche, doublons et notifications | Terminée, commit dédié |
| 5 | Menu, démonstration, intégration et tests | Terminée, commit dédié |

Dépôt Git **local**, branche `main`, sans dépôt distant configuré.
Les fichiers IntelliJ locaux, caches et sorties de compilation sont exclus du suivi.
Pour consulter les étapes : `git log --oneline --reverse`.

## Configuration

Kotlin/JVM **2.2.10**, Gradle Wrapper **8.14.3** (distribution vérifiée par SHA-256), JDK/cible JVM **21**.
Point d'entrée : `projet01_helpdesk.app.MainKt`.

Le projet utilise l'adresse officielle alternative `https://repo1.maven.org/maven2` de Maven Central,
car la résolution réseau de `repo.maven.apache.org` échouait lors de la préparation.

Références : [configuration Kotlin/Gradle](https://kotlinlang.org/docs/gradle-configure-project.html)
et [Gradle 8.14.3](https://docs.gradle.org/8.14.3/release-notes.html).
