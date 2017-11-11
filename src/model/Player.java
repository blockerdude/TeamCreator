package model;

public class Player {

    private int athleticism;
    private int throwing;
    private int experience;
    private int id;
    private String name;
    private String baggageCode;
    private GamesMissing gamesMissing;
    private Sex sex;

    /**
     * The base model of an individual player for Ultimate Frisbee.
     * @param athleticism athletic rating on a scale, 1-7
     * @param throwing throwing rating on a scale, 1-7
     * @param experience experience rating on a scale, 1-7
     * @param id unique identifier for this player
     * @param name given name for a player
     * @param baggageCode a baggage code given to link this player to other players
     * @param gamesMissing the amount of games the player expects to miss
     * @param sex the given sex of the player
     */
    public Player(int athleticism, int throwing, int experience, int id, String name, String baggageCode, GamesMissing gamesMissing, Sex sex) {
        this.athleticism = athleticism;
        this.throwing = throwing;
        this.experience = experience;
        this.id = id;
        this.name = name;
        this.baggageCode = baggageCode;
        this.gamesMissing = gamesMissing;
        this.sex = sex;
    }

    public int getAthleticism() {
        return athleticism;
    }

    public void setAthleticism(int athleticism) {
        this.athleticism = athleticism;
    }

    public int getThrowing() {
        return throwing;
    }

    public void setThrowing(int throwing) {
        this.throwing = throwing;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaggageCode() {
        return baggageCode;
    }

    public void setBaggageCode(String baggageCode) {
        this.baggageCode = baggageCode;
    }

    public GamesMissing getGamesMissing() {
        return gamesMissing;
    }

    public void setGamesMissing(GamesMissing gamesMissing) {
        this.gamesMissing = gamesMissing;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public int getAggregateScore(){
        return athleticism + experience + throwing;
    }

    public boolean equals(Object other) {
        return other instanceof Player && this.id == ((Player) other).id;
    }
}
