package model;

/**
 * An enum that describes how many games a player is likely to miss.
 * Will likely be changed to a raw int.
 * Is currently not in use.
 */
public enum GamesMissing {

    zeroToTwo(1),
    threeToFour(3.5),
    fiveToSix(5.5),
    mostMondays(8),
    mostTuesdays(8),
    mostWednesdays(8),
    mostThursdays(8);

    private final double averageGamesMissing;

    GamesMissing(double averageGamesMissing) {
        this.averageGamesMissing = averageGamesMissing;
    }

    public double getAverageGamesMissing() {
        return averageGamesMissing;
    }
}
