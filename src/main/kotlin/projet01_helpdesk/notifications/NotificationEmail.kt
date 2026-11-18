package projet01_helpdesk.notifications

class NotificationEmail : Notificateur {
    override fun envoyer(message: String) {
        require(message.isNotBlank()) { "Le message est obligatoire." }
        println("[Email simulé] $message")
    }
}
