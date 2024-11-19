package im.api.model.input.query

import im.api.model.input.validators.IsBool

data class UserChannelsFilterInputModel(
    @field:IsBool
    val filterOwned: String = "false",
)
