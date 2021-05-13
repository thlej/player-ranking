package fr.tle.infrastructure.persistence.memory

import fr.tle.domain.Player
import fr.tle.domain.PlayerRepository
import fr.tle.domain.RankedPlayer
import fr.tle.infrastructure.exception.PlayerAlreadyExistsException

class InMemoryPlayerRepository: PlayerRepository {
    val storage = mutableListOf<Player>()

    override fun add(player: Player): RankedPlayer {
        if(storage.any { it.pseudo == player.pseudo }) throw PlayerAlreadyExistsException()
        storage.add(player)
        return by(player.pseudo)!!
    }

    override fun update(player: Player): RankedPlayer? {
        val playerIdx = storage.indexOfFirst { it.pseudo == player.pseudo }
        if(playerIdx == -1) return null
        storage[playerIdx] = player
        return by(player.pseudo)!!
    }

    override fun by(pseudo: String): RankedPlayer? {
        return allSortedByRank().find { it.player.pseudo == pseudo }
    }

    override fun allSortedByRank(): Collection<RankedPlayer> {
        return storage
            .sortedByDescending { it.points }
            .mapIndexed{ idx, player -> RankedPlayer(player, idx+1) }
    }

    override fun deleteAll() {
        storage.clear()
    }
}