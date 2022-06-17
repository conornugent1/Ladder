package org.example;

import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.util.Precision;
import org.example.component.Ladder;
import org.example.component.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Unit test for simple App.
 */
public class LadderAppTest
{
    private Ladder ttLadder = new Ladder();
    private List<Pair<String,String>> playerNames = new ArrayList<>();

    @Before
    public void createLadder() {
        ttLadder.removeAllPlayers();
        playerNames.clear();

        List<Player> players = new ArrayList<>();
        playerNames.add(Pair.create("A",""));
        playerNames.add(Pair.create("B",""));
        playerNames.add(Pair.create("C",""));
        playerNames.add(Pair.create("D",""));
        playerNames.add(Pair.create("E",""));
        playerNames.add(Pair.create("F",""));
        playerNames.add(Pair.create("G",""));

        int position = 1;

        for (Pair<String,String> name: playerNames) {
            players.add(new Player(name.getFirst(), "", position++));
        }

        ttLadder.addPlayers(players);

        System.out.println("Initial Ladder");
        ttLadder.dumpLadder();
    }

    @Test
    public void insertPlayerTest() {
        System.out.println("Adding Player \"Conor\" rank: 3");
        ttLadder.insertPlayer(new Player("Conor", "", 3));
        ttLadder.dumpLadder();
        assert(ttLadder.getPlayer(3).getFirstName().equalsIgnoreCase("Conor"));
    }

    @Test
    public void removePlayerTest() {
        System.out.println("Removing Player \"D\"");
        int position = ttLadder.getPlayerPosition("D");
        Player nextPlayer = ttLadder.getPlayer(position+1); // player below player "D"
        ttLadder.removePlayer("D");
        assert(nextPlayer.getCurrentPosition() == position); // Assuming "D" has 1 player below him!
        ttLadder.dumpLadder();
    }

    @Test
    public void challengeMatchTest_1() {
        playMatch("E","C");
        ttLadder.dumpLadder();
    }

    @Test
    public void challengeMatchTest_2() {
        playMatch("F","E");
        ttLadder.dumpLadder();
    }

    @Test
    public void challengeMatchTest_3() {
        playMatch("F","A");
        ttLadder.dumpLadder();
    }

    @Test
    public void challengeMatchTest_4() {
        playMatch("F","A");
        playMatch("D","A");
        playMatch("E","A");
        playMatch("E","F");
        // NOTE - "D" was moved down 1 place as a result of "E" beating "F"
        // so his currPosition == 3 but this doesnt appear in his timeline [5,2] ie "3"
        // isnt present due to it being a bump down rather than a match result
        ttLadder.dumpLadder();
    }

    //Mix of results where higher beats lower & vice versa
    @Test
    public void challengeMatchTest_5() {
        playMatch("A","F");
        playMatch("A","B");
        playMatch("E","D");
        playMatch("E","F");
        ttLadder.dumpLadder();
    }

    /**
     * Random players play each other
     * If higher placed player beats lower player then no position swap
     * but history will be updated
     */
    @Test
    public void challengeMatchTest_6() {
        int numMatches = 500;
        /**
         * try to give higher ranked player a higher probability of winning the match
         * so get a weighted probability distribution where roughly 90% of the
         * time higher player wins
         */
        int [] weightedProbs = getWeightedProbabilities(numMatches);

        for (int i=0; i<numMatches; ++i) {
            int randomPlayer1 = ThreadLocalRandom.current().nextInt(1,playerNames.size()+1);
            int randomPlayer2 = ThreadLocalRandom.current().nextInt(1,playerNames.size()+1);

            Player rp1 = ttLadder.getPlayer(randomPlayer1); // ladder is 1-based
            Player rp2 = ttLadder.getPlayer(randomPlayer2);

            Player higherRankedPlayer = rp1.getCurrentPosition() < rp2.getCurrentPosition()?rp1:rp2;
            Player lowerRankedPlayer = rp1.getCurrentPosition() >= rp2.getCurrentPosition()?rp1:rp2;

            // exclude any player playing him/her self
            if (rp1.getCurrentPosition() != rp2.getCurrentPosition()) {
                if (weightedProbs[i] == 1) {
                    // higher ranked player has won
                    playMatch(higherRankedPlayer.getFirstName(), lowerRankedPlayer.getFirstName());
                }
                else {
                    // lower ranked player has won
                    playMatch(lowerRankedPlayer.getFirstName(), higherRankedPlayer.getFirstName());
                }
            }
        }
        ttLadder.dumpLadder();

    }

    /**
     * Assign the same winning probability to higher ranked players
     * in theory, could give individual players weighted win probabilities for more
     * accurate distribution
     */
    private int[] getWeightedProbabilities(int numSamples) {
        // see https://stackoverflow.com/questions/16435639/generating-random-integers-within-range-with-a-probability-distribution
        int[] numsToGenerate = new int[] {1, 2}; // 1=higher player, 2=lower player
        double[] discreteProbabilities = new double[] {0.90, 0.10};

        EnumeratedIntegerDistribution distribution =
                new EnumeratedIntegerDistribution(numsToGenerate, discreteProbabilities);

        int[] samples = distribution.sample(numSamples);
        double numOnes = 0;
        double numTwos = 0;
        for (int i=0; i<numSamples; i++) {
            if (samples[i] == 1) {
                numOnes++;
            }
            if (samples[i] == 2) {
                numTwos++;
            }
        }
        System.out.println();
        System.out.println("Num of \"1\": "+numOnes+", num of \"2\": "+numTwos);
        double winProb = numOnes/(numOnes + numTwos);
        winProb = Precision.round(winProb, 3)*100;
        System.out.println("Giving higher ranked player a prob of: "+winProb+"% winning");
        System.out.println();
        return samples;
     }

     private void playMatch(String winnerName, String loserName) {
         System.out.println("Player \""+winnerName+"\" beat \""+loserName +"\"");

         // These positions are before the swop
         int winnerPos = ttLadder.getPlayerPosition(winnerName);
         int loserPos = ttLadder.getPlayerPosition(loserName);

         ttLadder.matchResult(loserPos, winnerPos);

         //assume winner is higher ranked player unless proven otherwise
         //ie no change in position
         Player winner = ttLadder.getPlayer(winnerPos);
         Player loser = ttLadder.getPlayer(loserPos);

         if (winnerPos > loserPos) {
             // if winner is lower ranked player then winner will get loserPos,
             // loser will get loserPos+1
             winner = ttLadder.getPlayer(loserPos);
             loser = ttLadder.getPlayer(loserPos+1);
             assert(winner.getCurrentPosition() == loserPos &&
                     loser.getCurrentPosition() == loserPos+1);
             return;
         }

         // if higher ranked player wins
         assert(winner.getCurrentPosition() < loser.getCurrentPosition());

     }
}






