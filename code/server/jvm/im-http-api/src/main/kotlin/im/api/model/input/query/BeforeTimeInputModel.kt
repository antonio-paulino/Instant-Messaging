package im.api.model.input.query

import jakarta.validation.Valid
import jakarta.validation.constraints.PastOrPresent
import org.springframework.format.annotation.DateTimeFormat

/**
 * Input model for querying items before a specific time of creation.
 *
 * This is used to ensure that the query is limited to items created before a specific time,
 * which is useful for paginating through a list of items while ensuring that no items are skipped or duplicated.
 *
 * @property before The time before which to query items.
 */
data class BeforeTimeInputModel(
    @Valid
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @PastOrPresent
    val before: String?,
)
