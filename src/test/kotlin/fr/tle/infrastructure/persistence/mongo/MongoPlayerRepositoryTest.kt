package fr.tle.infrastructure.persistence.mongo

import com.mongodb.client.MongoDatabase
import fr.tle.domain.Player
import fr.tle.domain.RankedPlayer
import fr.tle.extensions.Database
import fr.tle.infrastructure.exception.PlayerAlreadyExistsException
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

@Database
class MongoPlayerRepositoryTest(mongoDatabase: MongoDatabase) : WithAssertions {

    private val collection = mongoDatabase.getCollection<Player>("players")
    private val repository = MongoPlayerRepository(collection)

    @Test
    fun `should add a player`() {
        val expected = Player("bill", 1)
        repository.add(expected)
        val found = collection.findOne(Player::pseudo eq "bill")
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
        val updatedRankedPlayer = repository.update(expected)
        with(updatedRankedPlayer!!){
            assertThat(player).isEqualTo(expected)
            assertThat(rank).isEqualTo(1)
            val found = collection.findOne(Player::pseudo eq "bill")
            assertThat(found).isEqualTo(expected)
        }
    }

    @Test
    fun `should not update unknown player's points`() {
        assertThat(repository.update(Player("foo", 666))).isNull()
        val found = collection.findOne(Player::pseudo eq "foo")
        assertThat(found).isNull()
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

        val allPlayers = repository.allSortedByRank()
        assertThat(allPlayers).isEmpty()
    }

    @AfterEach
    private fun clear(){
        collection.deleteMany()
    }
}