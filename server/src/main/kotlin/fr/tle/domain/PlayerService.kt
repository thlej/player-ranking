package fr.tle.domain

open class PlayerService(private val repository: PlayerRepository) {
    fun add(player: Player): RankedPlayer {
        repository.add(player)
        return repository.by(player.pseudo)
            ?: throw IllegalStateException("Player ${player.pseudo} cannot be found although it was successfully created")
    }

    fun update(player: Player): RankedPlayer? {
        repository.update(player)
        return repository.by(player.pseudo)
    }

    fun by(pseudo: String): RankedPlayer? {
        return repository.by(pseudo)
    }

    fun allSortedByRank(): Collection<RankedPlayer> {
        return repository.allSortedByRank()
    }

    fun deleteAll() {
        repository.deleteAll()
    }
}