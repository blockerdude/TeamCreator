package model;

public enum GamesMissing {

    zeroToTwo(1),
    threeToFour(3.5),
    fiveToSix(5.5),
    mostMondays(8),
    mostTuesdays(8),
    mostWednesdays(8),
    mostThursdays(8);


    private double averageGamesMissing;
    GamesMissing(double averageGamesMissing){
        this.averageGamesMissing = averageGamesMissing;
    }
    public double getAverageGamesMissing(){
        return averageGamesMissing;
    }
}
