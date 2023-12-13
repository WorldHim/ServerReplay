package me.senseiwells.replay.player.predicates

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class InTeamPredicate(
    val teams: List<String>
): ReplayPlayerPredicate(id) {
    override fun shouldRecord(context: ReplayPlayerContext): Boolean {
        val team = context.team?.name ?: return false
        return this.teams.contains(team)
    }

    override fun serialiseAdditional(json: JsonObject) {
        val teams = JsonArray()
        for (team in this.teams) {
            teams.add(team)
        }
        json.add("teams", teams)
    }

    companion object: PredicateFactory {
        override val id = "in_team"

        override fun create(data: JsonObject): ReplayPlayerPredicate {
            return InTeamPredicate(data.getAsJsonArray("teams").map { it.asString })
        }
    }
}