package channel

enum class ChannelRole {
    OWNER,
    MEMBER, // read-write
    GUEST // read-only
}