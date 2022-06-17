package org.example.component;

import org.apache.commons.math3.util.Pair;
import org.example.MatchStatus;
import org.example.stats.Colours;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Ladder {
    PriorityQueue<Player> pQueue = new PriorityQueue<>();
    AllPlayerStats allPlayerStats = new AllPlayerStats();

    private final int maxChallengeRange = 2;

    public void addPlayers(List<Player> players) {
        for (Player player : players) {
            addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        pQueue.add(player);
        allPlayerStats.addNewPlayer(player);
    }

    public void insertPlayer(Player newPlayer) {
        addPlayer(newPlayer);
        PriorityQueue<Player> pQueueCopy = new PriorityQueue<>(pQueue);

        // Need to find all players below newPlayer and push them down one
        while (!pQueueCopy.isEmpty()) {
            Player p = pQueueCopy.poll();
            if (newPlayer.getCurrentPosition() > p.getCurrentPosition()) {
                continue; // skip over the players positioned higher than the new player
            } else if (newPlayer.getCurrentPosition() == p.getCurrentPosition()
                    && newPlayer.getFirstName().equalsIgnoreCase(p.getFirstName())
                    && newPlayer.getLastName().equalsIgnoreCase(p.getLastName())) {
                continue; // found the new player
            }
            // Need to reset the position of all players under the new player
            p.setCurrentPosition(p.getCurrentPosition() + 1);
        }
        resetQueue(); // resort the queue
    }

    public void removePlayer(String firstName/*, String secondName, int currentPosition*/) {
        ArrayList<Player> playersList = new ArrayList<>(pQueue.size());

        while (!pQueue.isEmpty()) {
            Player p = pQueue.poll(); // NOTE, this will empty our pQueue!
            if (p.getFirstName().equalsIgnoreCase(firstName)) {  //TODO check both 1st & 2nd names
                continue;              // player is filtered out of arrayList
            }
            playersList.add(p);
        }

        // player is removed from playersList, need to move positions up now
        int rank = 1;
        for (Player p : playersList) {
            if (p.getCurrentPosition() > rank) {
                p.setCurrentPosition(p.getCurrentPosition() - 1); // This will find the gap in the list
            }
            rank++;
            pQueue.add(p); // add player back into pQueue
        }
        resetQueue(); //resort the queue  - is it needed in removal?
    }

    public void dumpLadder() {
        PriorityQueue<Player> pQueueCopy = new PriorityQueue<>(pQueue);
        System.out.println();
        System.out.println("== Ladder ==");
        while (!pQueueCopy.isEmpty()) {
            Player currentPlayer = pQueueCopy.poll();
            String stats1 = allPlayerStats.playerWinsLossesToString(currentPlayer);

            // String stats2 = allPlayerStats.playerHistoryAgainstOpponent(p, getPlayer(4));

            List<Player> opponents = getAllPlayers();
            opponents.remove(currentPlayer);

            String stats5 = allPlayerStats.getAllOpponentsHistory(currentPlayer, opponents);
            int allTimeHighPosition = allPlayerStats.getAllTimeHighestPosition(currentPlayer);
            double avgOpponentPosition = allPlayerStats.getAverageOpponentPosition(currentPlayer);
            Pair<String, Integer> highestOpponent = allPlayerStats.getHighestOpponentDefeated(currentPlayer);

            String highestOppStr = "highestOpp:" + highestOpponent.getFirst()+","+highestOpponent.getSecond();

            System.out.println(currentPlayer.getCurrentPosition()+": "
                            + currentPlayer.getFirstName()
                            + currentPlayer.getLastName()
                            + " " + stats1
                            + " History " + stats5
                            + " AllTimeHigh: " + allTimeHighPosition
                            + " AvgOppPos: " + avgOpponentPosition
                            + (!highestOpponent.getFirst().isEmpty()?highestOppStr:""));

            Pair<List<Integer>, List<Integer>> positions = allPlayerStats.getPositionTimeline(currentPlayer);
            String timeline = getHilightedBestStreak(positions);
            if (!timeline.isEmpty()) {
                System.out.println(timeline);
                System.out.println();
            }
        }

        // don't double count matches eg "A" vs "B" is 1 match but "B" vs "A" is the same match
        System.out.println("Total Num of matches: " + allPlayerStats.getTotalMatchCount()/2);
        System.out.println();
    }

    /**
     * LHS array is full timeline of how player position changed after each match
     * RHS is the longest (latest) best winning streak
     * @param positions
     * @return
     */
    private String getHilightedBestStreak(Pair<List<Integer>, List<Integer>> positions) {
        if (!positions.getFirst().isEmpty()) {
            List<Integer> timeline = positions.getFirst();
            List<Integer> streak = positions.getSecond();

            StringBuffer sb = new StringBuffer();
            int streakStart = streak.get(0);
            int streakEnd = streak.get(streak.size()-1);
            sb.append("[");

            for (int position = 0; position < timeline.size(); position++) {
                String bgColor = Colours.ANSI_BLUE;

                if (position >= streakStart && position <= streakEnd) {
                    //overwrite bgColor with winning streak entries
                    bgColor = Colours.ANSI_GREEN_BACKGROUND;
                }

                if (position == 0) {
                    sb.append(bgColor + timeline.get(position) + Colours.ANSI_RESET);
                }
                else {
                    sb.append(bgColor + ", " + timeline.get(position) + Colours.ANSI_RESET);
                }
            }
            sb.append("]");

            return("Position Timeline: " + sb.toString());
        }
        return "";
    }


    /**
     * Updates the match stats for each player and will also update the ladder positions
     * only if lower ranked player beat higher ranked player, otherwise ladder positions
     * are unchanged
     *
     * If a ladder update is needed, this func will swap the winners current (low) position
     * with the losers current (high) position and bump the loser down one place. It also bumps
     * any intermediate players between winner & loser down by one place
     *
     * @param loserCurrentPosition
     * @param winnerCurrentPosition
     */
    public void matchResult(int loserCurrentPosition, int winnerCurrentPosition) {
        // Retrieve the Players using their current positions
        Player pWinner = getPlayer(winnerCurrentPosition);
        Player pLoser = getPlayer(loserCurrentPosition);

        // Did the higher ranked player win?
        if (pWinner.getCurrentPosition() < pLoser.getCurrentPosition()) {
            // Winner is the higher ranked player, therefore no position swap is needed
            // but update the match stats
            allPlayerStats.updatePlayerStats(pWinner, MatchStatus.VICTORY, pLoser);
            allPlayerStats.updatePlayerStats(pLoser, MatchStatus.DEFEAT, pWinner);
            return;
        }

        // update their previous positions before adjusting their new positions
        pWinner.setPreviousPosition(winnerCurrentPosition);
        pLoser.setPreviousPosition(loserCurrentPosition);

        // get all intermediate players beginning with loser
        int midRange = loserCurrentPosition;
        List<Player> midRangePlayers = new ArrayList<>();
        while (midRange != winnerCurrentPosition) {
            Player midRangePlayer = getPlayer(midRange);
            midRangePlayers.add(midRangePlayer);
            midRange++;
        }

        // Only after we've got all the players impacted by the match result
        // we can then start changing player positions
        // - ie bump up winner
        pWinner.setCurrentPosition(loserCurrentPosition);

        // then bump midrange players down (includes the loser)
        for (Player p: midRangePlayers) {
            p.setCurrentPosition(p.getCurrentPosition() + 1);
        }

        resetQueue(); // resort the queue, otherwise its out of sync

        // update match results per player - only need 1 call as can set loser stats to stats to lost & v/v
        allPlayerStats.updatePlayerStats(pWinner, MatchStatus.VICTORY, pLoser);
        allPlayerStats.updatePlayerStats(pLoser, MatchStatus.DEFEAT, pWinner);
    }

    public int getPlayerPosition(String firstName/*, String lastName, String email*/) {
        PriorityQueue<Player> pQueueCopy = new PriorityQueue<>(pQueue);
        while (!pQueueCopy.isEmpty()) {
            Player p = pQueueCopy.poll();
            if (p.getFirstName().equalsIgnoreCase(firstName)) {
                return p.getCurrentPosition();
            }
        }
        return 0; // if player is not found, zero is invalid value
    }

    public Player getPlayer(int position) {
        PriorityQueue<Player> pQueueCopy = new PriorityQueue<>(pQueue);
        while (!pQueueCopy.isEmpty()) {
            Player p = pQueueCopy.poll();
            if (p.getCurrentPosition() == position) {
                return p;
            }
        }
        return null;
    }

    /** Turns out the players are only sorted when you add a player, can't just change the
     * priority and expect the pQueue to auto sort -
     * https://stackoverflow.com/questions/1871253/updating-java-priorityqueue-when-its-elements-change-priority
     * This func will drain the queue into a list then re-add into queue
     */
    private void resetQueue() {
        List<Player> players = new ArrayList<>(pQueue.size());
        while (!pQueue.isEmpty()) {
            players.add(pQueue.poll());
        }
        for (Player p: players) {
            pQueue.add(p);
        }
    }

    public void removeAllPlayers() {
        while (!pQueue.isEmpty()) {
            Player p = pQueue.poll();
            allPlayerStats.removePlayerStats(p);
        }
    }

    private List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        PriorityQueue<Player> pQueueCopy = new PriorityQueue<>(pQueue);
        while (!pQueueCopy.isEmpty()) {
            Player p = pQueueCopy.poll();
            players.add(p);
        }

        return players;
    }
}




