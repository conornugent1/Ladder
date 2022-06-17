package org.example.component;

public class Player implements Comparable<Player>{
    private String firstName;
    private String lastName;
    private int currentPosition;
    private int previousPosition;
    private String email;

    public Player(String firstName, String lastName, int currentPosition) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.currentPosition = currentPosition;
        // When 1st created, player's prev position doesn't exist so init to currPosition
        previousPosition = currentPosition;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void setPreviousPosition(int previousPosition) {
        this.previousPosition = previousPosition; // should it be equal to currentPosition
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getPreviousPosition() {
        return previousPosition;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int compareTo(Player player) {
        return Integer.compare(currentPosition, player.currentPosition);
    }
}
