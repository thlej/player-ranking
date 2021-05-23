package fr.tle.infrastructure.persistence.memory

import fr.tle.domain.Player
import fr.tle.domain.RankedPlayer
import fr.tle.infrastructure.exception.PlayerAlreadyExistsException
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class InMemoryPlayerRepositoryTest : WithAssertions {

    private val repository = InMemoryPlayerRepository()

    @Test
    fun `should add a player`() {
        val expected = Player("bill", 1)
        repository.add(expected)
        val found = repository.storage.first()
        assertThat(found).isEqualTo(expected)
    }

    @Test
    fun `should not add player if pseudo already exists`() {
        val player = Player("bill", 1)
        repository.add(player)
        assertThrows<PlayerAlreadyExistsException> { repository.add(player) }
    }

    @Test
    fun `should update given player's points`() {
        val basePlayer = Player("bill", 1)
        repository.add(basePlayer)
        val expected = basePlayer.copy(points = 100)
        repository.update(expected)
        val found = repository.storage.find { it.pseudo == "bill" }
        assertThat(found).isEqualTo(expected)
    }

    @Test
    fun `should not update unknown player's points`() {
        repository.update(Player("foo", 666))
        assertThat(repository.storage.find { it.pseudo == "foo" }).isNull()
    }

    @Test
    fun `should find an existing player`() {
        val expected = Player("bill", 1)
        repository.add(expected)

        val found = repository.by("bill")
        assertThat(found).isEqualTo(RankedPlayer(expected, 1))
    }

    @Test
    fun `should not find an unknown player`() {
        assertThat(repository.by("foo")).isNull()
    }

    @Test
    fun `should list all players sorted by descending rank`() {
        repository.add(Player("bill", 1))
        repository.add(Player("bob", 5))
        repository.add(Player("john", 10))

        val sortedByRank = repository.allSortedByRank()
        assertThat(sortedByRank).containsExactly(
            RankedPlayer(Player("john", 10), 1),
            RankedPlayer(Player("bob", 5), 2),
            RankedPlayer(Player("bill", 1), 3)
        )
    }

    @Test
    fun `should delete all players`() {
        repository.add(Player("bill", 1))
        repository.add(Player("bob", 5))
        repository.add(Player("john", 10))

        repository.deleteAll()

        assertThat(repository.storage).isEmpty()
    }

    @AfterEach
    internal fun tearDown() {
        repository.storage.clear()
    }
}