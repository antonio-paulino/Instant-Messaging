package im.api.model.output

import im.domain.wrappers.identifier.Identifier

class IdentifierOutputModel(
    val id: Long,
) {
    companion object {
        fun fromDomain(id: Identifier): IdentifierOutputModel =
            IdentifierOutputModel(
                id = id.value,
            )
    }
}
