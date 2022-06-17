package org.example.component;

import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.util.Precision;
import org.example.MatchStatus;
import org.example.stats.OpponentResult;

import java.util.*;

public class AllPlayerStats {

    // Map<Player, MatchResults> playerStats = new HashMap<>(); // cant use Map if player is changed
    List<Pair<Player, OpponentResults>> playerStats = new ArrayList<>();

    private OpponentResults getResults(Player p) {
        for (Pair<Player, OpponentResults> stats: playerStats) {
            if (stats.getFirst() == p) {
                return stats.getSecond();
            }
        }
        return null;
    }

    public void updatePlayerStats(Player player, MatchStatus status, Player opponent) {
        OpponentResults results = getResults(player);
        results.updateResult(player, status, opponent);
    }

    // Assign an empty MatchResults to player
    public void addNewPlayer(Player player) {
        OpponentResults results = new OpponentResults();
        playerStats.add(Pair.create(player, results));
    }

    public OpponentResults getPlayerStats(Player player) {
        return new OpponentResults();
    }

    public Pair<Integer, Integer> getPlayerWinsLosses(Player player) {
        OpponentResults results = getResults(player);
        return results.getWinsLosses();
    }

    public String playerWinsLossesToString(Player player) {
        Pair<Integer, Integer> winsLosses = getPlayerWinsLosses(player);
        int numWins = winsLosses.getFirst();
        int numLosses = winsLosses.getSecond();
        int numMatches = numWins+numLosses;
        return "|P:" + numMatches + " W:" +numWins + " L:" + numLosses + "|";
    }

    public String playerHistoryAgainstOpponent(Player player, Player opponent) {
        OpponentResults results = getResults(player);
        List<OpponentResult> stats = results.getStatsAgainstPlayer(opponent);

        int numWins=0;
        int numLosses=0;

        for (OpponentResult data: stats) {
            if (data.getStatus() == MatchStatus.VICTORY) {
                numWins++;
            }
            else if (data.getStatus() == MatchStatus.DEFEAT) {
                numLosses++;
            }
        }
        return opponent.getFirstName()+opponent.getLastName()+
                ":W"+numWins + ",L"+ numLosses;
    }

    /**
     * This will return all the history of opponents played for a particular player
     * If a player has played the same opponent > 1 times, it will aggregate the results for
     * that opponent
     * @param player
     * @param opponents
     * @return
     */
    public String getAllOpponentsHistory(Player player, List<Player> opponents) {
        OpponentResults results = getResults(player);
        List<OpponentResult> stats = results.getStatsResults();

        // Use a map to to collect aggregate info so only have to pass around the loop of
        // opponentData just once
        Map<String, AggregateOpponentData> winsLosses = new HashMap<>();

        // First, create an empty AggregateOpponentData for each opponent
        for (Player opponent: opponents) {
            winsLosses.putIfAbsent(opponent.getFirstName() + opponent.getLastName(),
                    new AggregateOpponentData());
        }

        // Second, Aggregate all OpponentData sets
        // eg if player played "A" twice then add all results against "A" into
        // 1x AggregateOpponentData
        for (OpponentResult data : stats) {
            // only have to loop around once, cause using a map to collect info
            AggregateOpponentData info = winsLosses.get(data.getOpponentFirstName()
                    +data.getOpponentLastName());
            if (data.getStatus() == MatchStatus.VICTORY) {
                info.numWins++;
            }
            if (data.getStatus() == MatchStatus.DEFEAT) {
                info.numLosses++;
            }
        }

        // Third, create output string of history of opponents played against
        StringBuffer sb = new StringBuffer();
        for (Map.Entry winLoss : winsLosses.entrySet()) {
            AggregateOpponentData info = (AggregateOpponentData)winLoss.getValue();
            if (info.numLosses>0 || info.numWins>0) {
                // only collect opponent stats if played against them - TODO already done?
                sb.append(winLoss.getKey() + ":W" + info.numWins + ",L" + info.numLosses + " ");
            }
        }
        return sb.toString();
    }

    public void removePlayerStats(Player p) {
        playerStats.remove(p);
    }

    // loop around each player & collect their match stats
    public int getTotalMatchCount() {
        int totNumMatches = 0;
        for (Pair<Player, OpponentResults> playerData: playerStats) {
            totNumMatches += playerData.getSecond().getTotalNumOfMatches();
        }
        return totNumMatches;
    }

    /**
     * Returns the highest ladder position reached for all time against the
     * list of opponents
     * @return
     */
    public int getAllTimeHighestPosition(Player player) {
        // First, get highest of current position or previous position
        int allTimeHighPosition = player.getCurrentPosition();
        if (allTimeHighPosition > player.getPreviousPosition()) {
            allTimeHighPosition = player.getPreviousPosition();
        }

        // Second, go through all the historical records to see if there was a previous high higher
        // than the current highs
        OpponentResults results = getResults(player);
        List<OpponentResult> stats = results.getStatsResults();
        for (OpponentResult result: stats) {
            if (result.getMyCurrentPosition() < allTimeHighPosition) {
                allTimeHighPosition = result.getMyCurrentPosition();
            }
            if (result.getMyPreviousPosition() < allTimeHighPosition) {
                allTimeHighPosition = result.getMyPreviousPosition();
            }
        }
        return allTimeHighPosition;
    }

    /**
     * Get the average opponent position
     * @param player
     * @return double average opponent position
     */
    public double getAverageOpponentPosition(Player player) {
        double averagePosn = 0;

        OpponentResults results = getResults(player);
        List<OpponentResult> stats = results.getStatsResults();
        if (stats.isEmpty()) {
            return averagePosn;
        }

        for (OpponentResult result: stats) {
            averagePosn += result.getOpponentCurrentPosition();
        }

        averagePosn = averagePosn/stats.size();
        return Precision.round(averagePosn, 2);
    }

    /**
     * Get info of victory against the highest ranked opponent
     * @param player
     * @return Pair<opponent first/last name, int opponent position>
     */
    public Pair<String, Integer> getHighestOpponentDefeated(Player player) {
        int highestOppPosition = 1_000_000;
        String opponentFirstName = "";
        String opponentLastName = "";

        OpponentResults results = getResults(player);
        List<OpponentResult> stats = results.getStatsResults();
        if (stats.isEmpty()) {
            return Pair.create(opponentFirstName, highestOppPosition);
        }

        for (OpponentResult result: stats) {
            if (highestOppPosition > result.getOpponentPreviousPosition() && result.getStatus()==MatchStatus.VICTORY) {
                highestOppPosition = result.getOpponentPreviousPosition();
                opponentFirstName = result.getOpponentFirstName();
                opponentLastName = result.getOpponentLastName();
            }
        }
        return Pair.create(opponentFirstName + opponentLastName, highestOppPosition);
    }

    /**
     * Return  the full historical position timeline AND best winning streak
     * (eg how the player's position rose & fell after each match)
     * @param player
     * @return Pair<List<Integer>, List<Integer>>, first list is tiemline & 2nd is winning streak
     */
    public Pair<List<Integer>, List<Integer>> getPositionTimeline(Player player) {
        List<Integer> timeline = new ArrayList<>();

        OpponentResults results = getResults(player);
        List<OpponentResult> stats = results.getStatsResults();

        if (stats.isEmpty()) {
            return Pair.create(timeline, timeline); // empty lists
        }
        timeline.add(stats.get(0).getMyPreviousPosition()); // position where (s)he started on the ladder
        for (OpponentResult result: stats) {
            timeline.add(result.getMyCurrentPosition());
        }
        List<Integer> bestWinningStreak = getBestWinningStreak(timeline);

        return Pair.create(timeline,bestWinningStreak);
    }

    /**
     * Get best winning streak
     * if more than 1 winning streak of same size, then get the latest streak
     * @param timeline
     */
    private List<Integer> getBestWinningStreak(List<Integer> timeline) {
        // Find best winning streak, min value in collection represents highest position in ladder
        int startPosition = timeline.indexOf (Collections.min(timeline));
        int endPosition = timeline.lastIndexOf (Collections.min(timeline));
        // could be 1 or more gaps in best streak ie mini streaks,
        // need to find all mini streaks & get longest one

        // get all time highest ladder position all ministreaks will have this value
        int highestPosition = timeline.get(startPosition);
        Map<Integer, List<Integer>> winningStreaks = new HashMap<>();

        int numOfMiniStreaks=1; // have to have 1 at least

        for (int i = startPosition; i <= endPosition; i++) {
            if (timeline.get(i) == highestPosition) {
                if (!winningStreaks.containsKey(numOfMiniStreaks)) {
                    // create a new ministreak
                    List<Integer> miniStreak = new ArrayList<>();
                    winningStreaks.put(numOfMiniStreaks, miniStreak);
                }
                List<Integer> miniStreak = winningStreaks.get(numOfMiniStreaks);
                miniStreak.add(i);
            }
            else {
                // gap detected, so create another ministreak
                numOfMiniStreaks++;
            }
        }

        // which of the ministreaks is the longest?
        List<Integer> longestMiniStreak = null;
        int numOfHighestPositions = 0;
        for (Map.Entry<Integer,List<Integer>> entry : winningStreaks.entrySet()) {
            if (numOfHighestPositions <= entry.getValue().size()) {
                // NOTE the "<=" will ensure we get the latest mini streak (if more than 1 of the same size)
                // If we wanted just the 1st mini streak then use "<"
                longestMiniStreak = entry.getValue();
                numOfHighestPositions = entry.getValue().size();
            }
        }
        return longestMiniStreak;
    }

    private class AggregateOpponentData {
        private int numWins = 0;
        private int numLosses = 0;
    }
}










