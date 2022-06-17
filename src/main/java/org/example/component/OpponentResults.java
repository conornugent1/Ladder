package org.example.component;


import org.apache.commons.math3.util.Pair;
import org.example.MatchStatus;
import org.example.stats.OpponentResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Each player will have a history of match results (OpponentResults)
 * OpponentResults will contain a list of individual results against opponents (OpponentResult)
 * eg, player A played B,C,D ... etc with a full snapshot of time the match was player, opponent,
 * current and previous position etc
 */
public class OpponentResults {
    private List<OpponentResult> statsResults = new ArrayList<>();

    /**
     *  Add a new OponentResult to OpponentResults ie a summary of last match played
     * @param player
     * @param status
     * @param opponent
     */
    public void updateResult(Player player, MatchStatus status, Player opponent) {
        OpponentResult data = new OpponentResult(player, opponent, status);
        statsResults.add(data);
    }

    public Pair<Integer, Integer> getWinsLosses() {
        int numWins=0;
        int numLosses=0;
        for (OpponentResult data: statsResults)  {
            if (data.getStatus() == MatchStatus.VICTORY) {
                numWins++;
            }
            else if (data.getStatus() == MatchStatus.DEFEAT) {
                numLosses++;
            }
        }
        return Pair.create(numWins, numLosses);
    }

    /**
     * Return all the results against a particular player
     * (assumes no other player with same first.second name)
     * @param opponent
     * @return
     */
    public List<OpponentResult> getStatsAgainstPlayer(Player opponent) {
        List<OpponentResult> opponentResults = new ArrayList<>();

        for (OpponentResult data: statsResults) {
            if (data.getOpponentFirstName() == opponent.getFirstName() &&
                data.getOpponentLastName() == opponent.getLastName()) {
                opponentResults.add(data);
            }
        }
        return opponentResults;
    }

    /**
     * Return all match results for a player
     * @return
     */
    public List<OpponentResult> getStatsResults() {
        return statsResults;
    }

    // total num of matches faced by this player
    public int getTotalNumOfMatches() {
        return statsResults.size();
    }
}

