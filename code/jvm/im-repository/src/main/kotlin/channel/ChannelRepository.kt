package channel

import Repository

interface ChannelRepository: Repository<Channel, Long> {
    fun findByName(name: String): Channel?
}