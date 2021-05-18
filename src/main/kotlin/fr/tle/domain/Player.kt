package fr.tle.domain

data class Player(val pseudo: String, val points: Int) {
    init {
        require(pseudo.isNotBlank()) { "Player.pseudo should not be empty" }
        require(points >= 0) { "Player.points must be positive" }
    }
}

data class RankedPlayer(val player: Player, val rank: Int) {
    init {
        require(rank >= 0) { "RankedPlayer.rank must be positive" }
    }
}

class PlayerService(private val repository: PlayerRepository) { // TODO tests
    fun add(player: Player): RankedPlayer {
        repository.add(player)
        return repository.by(player.pseudo)
            ?: throw IllegalStateException("Player ${player.pseudo} cannot be found although it was successfully created") // FIXME better?
    }

    fun update(player: Player): RankedPlayer? {
        repository.update(player)
        return repository.by(player.pseudo)
//            ?: throw Exception("Player $player update failed, please retry later") // FIXME better?
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

interface PlayerRepository {
    fun add(player: Player)
    fun update(player: Player)
    fun by(pseudo: String): RankedPlayer?
    fun allSortedByRank(): Collection<RankedPlayer>
    fun deleteAll()
}