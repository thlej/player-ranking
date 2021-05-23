package fr.tle.interfaces.rest.mappers

import fr.tle.domain.Player
import fr.tle.domain.RankedPlayer
import fr.tle.interfaces.rest.dto.PlayerCreateRequest
import fr.tle.interfaces.rest.dto.PlayerResponse
import fr.tle.interfaces.rest.dto.RankedPlayerResponse
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test

class PlayerMappersTest : WithAssertions {

    @Test
    internal fun `should map PlayerCreateRequest to Player`() {
        val expected = Player("bob", 10)
        val mapped = PlayerCreateRequest("bob", 10).toPlayer()
        assertThat(mapped).isEqualTo(expected)
    }

    @Test
    internal fun `should map Player to PlayerResponse`() {
        val expected = PlayerResponse("bill", 9)
        val mapped = Player("bill", 9).toPlayerResponse()
        assertThat(mapped).isEqualTo(expected)
    }

    @Test
    internal fun `should map RankedPlayer to RankedPlayerResponse`() {
        val expected = RankedPlayerResponse("john", 666, 1)
        val mapped = RankedPlayer(Player("john", 666), 1).toRankedPlayerResponse()
        assertThat(mapped).isEqualTo(expected)
    }
}