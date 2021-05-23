package fr.tle.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PlayerServiceTest : WithAssertions {
    private val playerRepository = mockk<PlayerRepository>()
    private val playerService = PlayerService(playerRepository)

    @Test
    fun `should add a new player`() {
        val expectedPlayer = Player("bill", 10)
        val expectedRankedPlayer = RankedPlayer(expectedPlayer, 1)
        every { playerRepository.add(any()) } returns Unit
        every { playerRepository.by(any()) } returns expectedRankedPlayer

        val rankedPlayer = playerService.add(expectedPlayer)

        assertThat(rankedPlayer).isEqualTo(expectedRankedPlayer)
        verify { playerRepository.add(any()) }
    }

    @Test
    fun `should throw IllegalStateException when added player cannot be retrieved`() {
        val expectedPlayer = Player("bill", 10)
        every { playerRepository.add(any()) } returns Unit
        every { playerRepository.by(any()) } returns null

        val message = assertThrows<IllegalStateException> { playerService.add(expectedPlayer) }.message
        assertThat(message).isEqualTo("Player ${expectedPlayer.pseudo} cannot be found although it was successfully created")
        verify { playerRepository.add(any()) }
    }

    @Test
    fun `should update given player points`() {
        val expectedPlayer = Player("bill", 10)
        val expectedRankedPlayer = RankedPlayer(expectedPlayer, 1)
        every { playerRepository.update(any()) } returns Unit
        every { playerRepository.by(any()) } returns expectedRankedPlayer

        val rankedPlayer = playerService.update(expectedPlayer)
        assertThat(rankedPlayer).isEqualTo(expectedRankedPlayer)
        verify { playerRepository.update(any()) }
    }

    @Test
    fun `should get a player`() {
        val expectedPlayer = Player("bill", 10)
        val expectedRankedPlayer = RankedPlayer(expectedPlayer, 1)
        every { playerRepository.by(any()) } returns expectedRankedPlayer

        val rankedPlayer = playerService.by("bill")
        assertThat(rankedPlayer).isEqualTo(expectedRankedPlayer)
    }

    @Test
    fun `should list all players sorted by rank`() {
        val expectedPlayer = Player("bill", 10)
        val expectedRankedPlayer = RankedPlayer(expectedPlayer, 1)
        every { playerRepository.allSortedByRank() } returns listOf(expectedRankedPlayer)

        val rankedPlayers = playerService.allSortedByRank()
        assertThat(rankedPlayers).containsExactlyInAnyOrder(expectedRankedPlayer)
    }

    @Test
    fun `should delete all players`() {
        every { playerRepository.deleteAll() } returns Unit

        playerService.deleteAll()

        verify { playerRepository.deleteAll() }
    }
}