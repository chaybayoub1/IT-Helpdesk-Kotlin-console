package projet01_helpdesk.app

import projet01_helpdesk.notifications.NotificationEmail
import projet01_helpdesk.notifications.NotificationSMS
import projet01_helpdesk.services.ServiceSupport

fun main(args: Array<String>) {
    val options = setOf("--demo", "--sms", "--help")
    require(args.all { it in options }) { "Options disponibles : --demo, --sms, --help." }
    if ("--help" in args) {
        println("IT Helpdesk : menu interactif par défaut.")
        println("--demo : scénario complet ; --sms : notifications SMS simulées.")
        return
    }
    val notificateur = if ("--sms" in args) NotificationSMS() else NotificationEmail()
    if ("--demo" in args) {
        executerDemonstration(notificateur)
    } else {
        ConsoleHelpdesk(ServiceSupport(notificateur)).demarrer()
    }
}
