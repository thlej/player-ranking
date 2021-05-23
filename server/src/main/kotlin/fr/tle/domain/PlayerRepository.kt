package fr.tle.domain

interface PlayerRepository {
    fun add(player: Player)
    fun update(player: Player)
    fun by(pseudo: String): RankedPlayer?
    fun allSortedByRank(): Collection<RankedPlayer>
    fun deleteAll()
}