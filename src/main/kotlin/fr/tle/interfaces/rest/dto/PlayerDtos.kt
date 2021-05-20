package fr.tle.interfaces.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayerCreateRequest(val pseudo: String, val points: Int){
    init {
        require(pseudo.isNotBlank()) { "PlayerCreateRequest.pseudo should not be empty or blank" }
        require(points >= 0) { "PlayerCreateRequest.points must be positive" }
    }
}

@Serializable
data class PlayerResponse(val pseudo: String, val points: Int)

@Serializable
data class RankedPlayerResponse(val player: PlayerResponse, val rank: Int)

@Serializable
data class PlayerUpdateRequest(val points: Int){
    init {
        require(points >= 0) { "PlayerUpdateRequest.points must be positive" }
    }
}