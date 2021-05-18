package fr.tle.interfaces.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayerCreateRequest(val pseudo: String, val points: Int)

@Serializable
data class PlayerResponse(val pseudo: String, val points: Int)

@Serializable
data class RankedPlayerResponse(val player: PlayerResponse, val rank: Int)

@Serializable
data class PlayerUpdateRequest(val points: Int)