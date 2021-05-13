package fr.tle.infrastructure.persistence.mongo

import com.mongodb.client.MongoCollection
import fr.tle.domain.Player
import fr.tle.domain.PlayerRepository
import fr.tle.domain.RankedPlayer
import fr.tle.infrastructure.exception.PlayerAlreadyExistsException
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*

class MongoPlayerRepository(private val collection: MongoCollection<Player>) : PlayerRepository {
    override fun add(player: Player): RankedPlayer {
        if (collection.findOne(Player::pseudo eq player.pseudo) !== null) throw PlayerAlreadyExistsException()
        collection.insertOne(player)
        return by(player.pseudo)!! // FIXME better than '!!' ?
        // FIXME move this by() into PlayerService?
    }

    override fun update(player: Player): RankedPlayer? {
        collection.updateOne(Player::pseudo eq player.pseudo, player)
        return by(player.pseudo) // FIXME move this by() into PlayerService?
    }

    override fun by(pseudo: String): RankedPlayer? {
        return allSortedByRank().find { it.player.pseudo == pseudo } // TODO match on db side?
    }

    override fun allSortedByRank(): Collection<RankedPlayer> {
        return collection.aggregate<RankedPlayer>(
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
        ).toList()
    }

    override fun deleteAll() {
        collection.deleteMany()
    }
}