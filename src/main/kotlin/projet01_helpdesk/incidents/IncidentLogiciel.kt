package projet01_helpdesk.incidents

class IncidentLogiciel(description: String, val logiciel: String) : Incident(description) {
    init {
        require(logiciel.isNotBlank()) { "Le nom du logiciel est obligatoire." }
    }

    override fun diagnostiquer(): String =
        "Vérifier la configuration et les journaux de $logiciel."
}
