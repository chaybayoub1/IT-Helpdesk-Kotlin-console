package projet01_helpdesk.incidents

abstract class Incident(val description: String) {
    init {
        require(description.isNotBlank()) { "La description de l'incident est obligatoire." }
    }

    abstract fun diagnostiquer(): String
}
