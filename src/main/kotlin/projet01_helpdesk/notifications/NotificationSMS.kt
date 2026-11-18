package projet01_helpdesk.notifications

class NotificationSMS : Notificateur {
    override fun envoyer(message: String) {
        require(message.isNotBlank()) { "Le message est obligatoire." }
        println("[SMS simulé] $message")
    }
}
