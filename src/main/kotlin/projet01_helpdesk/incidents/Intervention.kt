package projet01_helpdesk.incidents

import java.time.LocalDateTime
import projet01_helpdesk.personnes.TechnicienSupport

data class Intervention(
    val description: String,
    val technicien: TechnicienSupport,
    val date: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(description.isNotBlank()) { "La description de l'intervention est obligatoire." }
    }
}
