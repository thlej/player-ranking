package fr.tle.interfaces.rest.dto

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PlayerDtosTest : WithAssertions{

    @Nested
    inner class PlayerCreateRequestTest{
        @Test
        internal fun `should instantiate PlayerCreateRequest`() {
            val playerCreateRequest = PlayerCreateRequest("bill", 10)
            assertThat(playerCreateRequest.pseudo).isEqualTo("bill")
            assertThat(playerCreateRequest.points).isEqualTo(10)
        }

        @Test
        internal fun `should not instantiate PlayerCreateRequest with blank pseudo`() {
            val message = assertThrows<IllegalArgumentException> { PlayerCreateRequest("   ", 1) }.message
            assertThat(message).isEqualTo("PlayerCreateRequest.pseudo should not be empty or blank")
        }

        @Test
        internal fun `should not instantiate Player with negative points`() {
            val message = assertThrows<IllegalArgumentException> { PlayerCreateRequest("bill", -10) }.message
            assertThat(message).isEqualTo("PlayerCreateRequest.points must be positive")
        }
    }

    @Nested
    inner class PlayerUpdateRequestTest{
        @Test
        internal fun `should instantiate PlayerUpdateRequestTest`() {
            val playerUpdateRequest = PlayerUpdateRequest(10)
            assertThat(playerUpdateRequest.points).isEqualTo(10)
        }

        @Test
        internal fun `should not instantiate PlayerUpdateRequestTest with negative points`() {
            val message = assertThrows<IllegalArgumentException> { PlayerUpdateRequest(-10) }.message
            assertThat(message).isEqualTo("PlayerUpdateRequest.points must be positive")
        }
    }
}