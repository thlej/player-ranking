package fr.tle.infrastructure.persistence.memory

import fr.tle.domain.Player
import fr.tle.domain.RankedPlayer
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class InMemoryPlayerRepositoryTest : WithAssertions {

    private val repository = InMemoryPlayerRepository()

    @Test
    fun `should add a player`() {
        assertThat(repository.storage).hasSize(3)
        val addedPlayer = repository.add(Player("qux", 100))
        assertThat(repository.storage).hasSize(4)
        assertThat(repository.storage[3]).isEqualTo(addedPlayer.player)
        assertThat(addedPlayer).isEqualTo(RankedPlayer(Player("qux", 100), 1))
    }

    @Test
    fun `should update given player's points`() {
        val updatedPlayer = repository.update(Player("foo", 11))!!
        assertThat(updatedPlayer.player).isEqualTo(Player("foo", 11))
        assertThat(updatedPlayer.rank).isEqualTo( 1)
    }

    @Test
    fun `should not update unknown player's points`() {
        assertThat(repository.update(Player("foobarbaz", 100))).isNull()
    }

    @Test
    fun `should find an existing player`() {
        val player = repository.by("foo")
        assertThat(player).isEqualTo(RankedPlayer(Player("foo", 10), 1))
    }

    @Test
    fun `should not find an unknown player`() {
        val unknownPlayer = repository.by("foobarbaz")
        assertThat(unknownPlayer).isNull()
    }

    /*@Test
    fun `should list all players sorted by ascending rank`() {
        val allPlayersByAscendingRank = repository.allSortedByRank()
        assertThat(allPlayersByAscendingRank).containsExactly(
            RankedPlayer(Player("foo", 10), 1),
            RankedPlayer(Player("bar", 5), 2),
            RankedPlayer(Player("baz", 1), 3),
        )
    }*/

    @Test
    fun `should list all players sorted by descending rank`() {
        val allPlayersByAscendingRank = repository.allSortedByRank()
        assertThat(allPlayersByAscendingRank).containsExactly(
            RankedPlayer(Player("foo", 10), 1),
            RankedPlayer(Player("bar", 5), 2),
            RankedPlayer(Player("baz", 1), 3),
        )
    }

    @Test
    fun `should delete all players`() {
        assertThat(repository.storage).hasSize(3)
        repository.deleteAll()
        assertThat(repository.storage).isEmpty()
    }
}