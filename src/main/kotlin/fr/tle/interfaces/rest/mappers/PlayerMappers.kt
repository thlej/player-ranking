package fr.tle.interfaces.rest.mappers

import fr.tle.domain.Player
import fr.tle.domain.RankedPlayer
import fr.tle.interfaces.rest.dto.PlayerCreateRequest
import fr.tle.interfaces.rest.dto.PlayerResponse
import fr.tle.interfaces.rest.dto.RankedPlayerResponse

fun PlayerCreateRequest.toPlayer() = Player(pseudo, points)
fun Player.toPlayerResponse() = PlayerResponse(pseudo, points)
fun RankedPlayer.toRankedPlayerResponse() = RankedPlayerResponse(player.toPlayerResponse(), rank)
