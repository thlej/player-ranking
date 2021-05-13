package fr.tle.domain

import kotlinx.serialization.Serializable

@Serializable // TODO move it to a DTO
data class Player(val pseudo: String, val points: Int){ // FIXME + id or unique pseudo?
    init {
        require(pseudo.isNotBlank()){"Player.pseudo should not be empty"}
        require(points >= 0){"Player.points must be positive"}
    }
}

@Serializable // TODO move it to a DTO
data class RankedPlayer(val player: Player, val rank: Int){
    init {
        require(rank >= 0){"RankedPlayer.rank must be positive"}
    }
}

class PlayerService(val repository: PlayerRepository){
    fun add(player: Player): RankedPlayer {
        return repository.add(player)
    }
    fun update(player: Player): RankedPlayer?{
        return repository.update(player)
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

interface PlayerRepository{
    fun add(player: Player): RankedPlayer
    fun update(player: Player): RankedPlayer?
    fun by(pseudo: String): RankedPlayer?
    fun allSortedByRank(): Collection<RankedPlayer>
    fun deleteAll()
}