package im.api.model.input.query

import im.api.model.input.wrappers.Identifier
import jakarta.validation.Valid

/**
 * Input model for querying entities after a specific identifier.
 *
 * Using this query input will ensure that the entities have identifiers greater than the specified identifier.
 * This is useful for paginating through a list of entities while ensuring that no entities are skipped or duplicated.
 *
 * @property after The identifier after which to query entities.
 */
data class AfterIdInputModel(
    @Valid
    val after: Identifier?,
)
