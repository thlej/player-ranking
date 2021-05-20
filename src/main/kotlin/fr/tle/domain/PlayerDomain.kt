package fr.tle.domain

data class Player(val pseudo: String, val points: Int) {
    init {
        require(pseudo.isNotBlank()) { "Player.pseudo should not be empty or blank" }
        require(points >= 0) { "Player.points must be positive" }
    }
}

data class RankedPlayer(val player: Player, val rank: Int) {
    init {
        require(rank >= 0) { "RankedPlayer.rank must be positive" }
    }
}