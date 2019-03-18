package com.ganwl.multiplayerwordgame;

// used to keep track of user game statistics and flash to them when game ends
public class GameStatistics {
    private int finalScore;
    private int highestStreaks;
    private int numberOfHintsUsed;
    private int numberOfSkipsUsed;
    private int totalNumberOfCorrectWords;

    // constructor
    public GameStatistics() {
        resetStatistics();
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public void checkAndSetHighestStreaks(int streak) {
        if(streak > highestStreaks) {
            highestStreaks = streak;
        }
    }

    public void incrementNumberOfHintsUsed() {
        this.numberOfHintsUsed++;
    }

    public void incrementNumberOfSkipsUsed() {
        this.numberOfSkipsUsed++;
    }

    public void incrementTotalNumberOfCorrectWords() {
        this.totalNumberOfCorrectWords++;
    }

    public void resetStatistics() {
        this.finalScore = 0;
        this.highestStreaks = 0;
        this.numberOfHintsUsed = 0;
        this.numberOfSkipsUsed = 0;
        this.totalNumberOfCorrectWords = 0;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public int getHighestStreaks() {
        return highestStreaks;
    }

    public int getNumberOfHintsUsed() {
        return numberOfHintsUsed;
    }

    public int getNumberOfSkipsUsed() {
        return numberOfSkipsUsed;
    }

    public int getTotalNumberOfCorrectWords() {
        return totalNumberOfCorrectWords;
    }
}
