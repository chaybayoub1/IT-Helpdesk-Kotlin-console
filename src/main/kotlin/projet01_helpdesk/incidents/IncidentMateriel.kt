package projet01_helpdesk.incidents

class IncidentMateriel(description: String) : Incident(description) {
    override fun diagnostiquer(): String =
        "Vérifier le matériel, l'alimentation et les connexions."
}
