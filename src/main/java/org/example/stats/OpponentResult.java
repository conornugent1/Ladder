package org.example.stats;

import org.example.MatchStatus;
import org.example.component.Player;

//import java.time.Instant;

public class OpponentResult {

    /**
     * we don't keep the opponent as a member var here because if opponent was
     * ever deleted from ladder then the stats woulld be out of sync
     * => ok to store opponents attributes (name, position etc)
     */
    private final String opponentFirstName;
    private final String opponentLastName;
    //private final Instant instant = Instant.now();
    private final MatchStatus status;
    private int opponentPreviousPosition;
    private int opponentCurrentPosition;
    private int myPreviousPosition;
    private int myCurrentPosition;

    public OpponentResult(Player player, Player opponent, MatchStatus status) {
        opponentFirstName = opponent.getFirstName();
        opponentLastName = opponent.getLastName();
        this.status = status;
        opponentPreviousPosition = opponent.getPreviousPosition();
        opponentCurrentPosition = opponent.getCurrentPosition();
        myPreviousPosition = player.getPreviousPosition();
        myCurrentPosition = player.getCurrentPosition();
    }

    public String getOpponentFirstName() {
        return opponentFirstName;
    }

    public String getOpponentLastName() {
        return opponentLastName;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public int getMyCurrentPosition() {
        return myCurrentPosition;
    }

    public int getMyPreviousPosition() {
        return myPreviousPosition;
    }

    public int getOpponentCurrentPosition() {
        return opponentCurrentPosition;
    }

    public int getOpponentPreviousPosition() {
        return opponentPreviousPosition;
    }
}
