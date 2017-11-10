package model;

public class SwapPair {

    private PlayerGroup low;
    private PlayerGroup high;
    private int difference;

    public SwapPair(PlayerGroup low, PlayerGroup high, int difference) {
        this.low = low;
        this.high = high;
        this.difference = difference;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    public PlayerGroup getHigh() {
        return high;
    }

    public void setHigh(PlayerGroup high) {
        this.high = high;
    }

    public PlayerGroup getLow() {
        return low;
    }

    public void setLow(PlayerGroup low) {
        this.low = low;
    }
}
