package model;

import java.util.List;

public class Player {

    private int athleticism;
    private int throwing;
    private int experience;
    private int id;
    private String name;
    private String baggageCode;
    private List<String> baggageIds;
    private GamesMissing gamesMissing;
    private Gender gender;

    public Player(int athleticism, int throwing, int experience, int id, String name, String baggageCode, List<String> baggageIds, GamesMissing gamesMissing, Gender gender) {
        this.athleticism = athleticism;
        this.throwing = throwing;
        this.experience = experience;
        this.id = id;
        this.name = name;
        this.baggageCode = baggageCode;
        this.baggageIds = baggageIds;
        this.gamesMissing = gamesMissing;
        this.gender = gender;
    }

    public void addBaggageId(String id){
        baggageIds.add(id);
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

    public List<String> getBaggageIds() {
        return baggageIds;
    }

    public void setBaggageIds(List<String> baggageIds) {
        this.baggageIds = baggageIds;
    }

    public GamesMissing getGamesMissing() {
        return gamesMissing;
    }

    public void setGamesMissing(GamesMissing gamesMissing) {
        this.gamesMissing = gamesMissing;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAggregateScore(){
        return athleticism + experience + throwing;
    }

    public boolean equals(Object other) {
        return other instanceof Player && this.id == ((Player) other).id;
    }
}
