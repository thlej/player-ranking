package fr.tle.domain

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class PlayerDomainTest : WithAssertions {

    @Nested
    inner class PlayerTest {
        @Test
        internal fun `should instantiate Player`() {
            val player = Player("bill", 99)
            assertThat(player.pseudo).isEqualTo("bill")
            assertThat(player.points).isEqualTo(99)
        }

        @Test
        internal fun `should not instantiate Player with blank pseudo`() {
            val message = assertThrows<IllegalArgumentException> { Player("   ", 1) }.message
            assertThat(message).isEqualTo("Player.pseudo should not be empty or blank")
        }

        @Test
        internal fun `should not instantiate Player with negative points`() {
            val message = assertThrows<IllegalArgumentException> { Player("bill", -10) }.message
            assertThat(message).isEqualTo("Player.points must be positive")
        }
    }

    @Nested
    inner class RankedPlayerTest {
        @Test
        internal fun `should instantiate RankedPlayer`() {
            val rankedPlayer = RankedPlayer(Player("bill", 99), 1)
            assertThat(rankedPlayer.player.pseudo).isEqualTo("bill")
            assertThat(rankedPlayer.player.points).isEqualTo(99)
            assertThat(rankedPlayer.rank).isEqualTo(1)
        }

        @Test
        internal fun `should not instantiate RankedPlayer with negative rank`() {
            val message = assertThrows<IllegalArgumentException> { RankedPlayer(Player("bill", 10), -1) }.message
            assertThat(message).isEqualTo("RankedPlayer.rank must be positive")
        }
    }
}