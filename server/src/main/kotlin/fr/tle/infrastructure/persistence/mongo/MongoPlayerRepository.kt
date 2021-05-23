package fr.tle.infrastructure.persistence.mongo

import com.mongodb.client.MongoCollection
import fr.tle.domain.Player
import fr.tle.domain.PlayerRepository
import fr.tle.domain.RankedPlayer
import fr.tle.infrastructure.exception.PlayerAlreadyExistsException
import kotlinx.serialization.Serializable
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*

class MongoPlayerRepository(private val collection: MongoCollection<PlayerDocument>) : PlayerRepository {
    override fun add(player: Player) {
        collection.findOne(Player::pseudo eq player.pseudo)?.let { throw PlayerAlreadyExistsException() }
        collection.insertOne(player.toPlayerDocument())
    }

    override fun update(player: Player) {
        collection.updateOne(Player::pseudo eq player.pseudo, player.toPlayerDocument())
    }

    override fun by(pseudo: String): RankedPlayer? {
        return allSortedByRank().find { it.player.pseudo == pseudo }
    }

    override fun allSortedByRank(): Collection<RankedPlayer> {
        return collection.aggregate<RankedPlayerDocument>(
            """
            [
                {
                    $sort: {
                    points: -1
                }
                },
                {
                    $group: {
                        _id: 1,
                        player: {
                            $push: {
                                pseudo: "$ pseudo",
                                points: "$ points"
                            }
                        }
                    }
                },
                { $unwind: { path: "$ player", includeArrayIndex: "rank" } },
                {
                    $project: {
                        _id: false,
                        player : {
                            pseudo: "$ player.pseudo",
                            points: "$ player.points"
                        }
                        rank: { $ toInt: { $ sum: ["$ rank", 1] } }
                    }
                }
            ]
            """.formatJson()
        ).toList().map { it.toRankedPlayer() }
    }

    override fun deleteAll() {
        collection.deleteMany()
    }
}

@Serializable
data class PlayerDocument(val pseudo: String, val points: Int)

@Serializable
data class RankedPlayerDocument(val player: PlayerDocument, val rank: Int)

fun Player.toPlayerDocument() = PlayerDocument(pseudo, points)
fun RankedPlayerDocument.toRankedPlayer() = RankedPlayer(Player(player.pseudo, player.points), rank)