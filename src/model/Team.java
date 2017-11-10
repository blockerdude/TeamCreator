package model;

import java.util.List;

public class Team {

    private String name;
    private SortedArrayList<PlayerGroup> playersGroups;
    private int desiredTeamSize;
    private int desiredNumberMales;
    private int desiredNumberFemales;

    public Team(String name, SortedArrayList<PlayerGroup> playersGroups, int desiredTeamSize, int desiredNumberMales, int desiredNumberFemales) {
        this.name = name;
        this.playersGroups = playersGroups;
        this.desiredTeamSize = desiredTeamSize;
        this.desiredNumberMales = desiredNumberMales;
        this.desiredNumberFemales = desiredNumberFemales;
    }

    public void addPlayerGroup(PlayerGroup pg){
        playersGroups.add(pg);
    }
    public void removePlayerGroup(PlayerGroup pg){
        playersGroups.remove(pg);

    }

    public int getNumberPlayers(){
        int count = 0;
        for (PlayerGroup group: playersGroups) {
            count += group.getPlayerCount();
        }
        return count;
    }

    public int getNumberMales(){
        int count = 0;
        for (PlayerGroup group: playersGroups) {
            count += group.getNumberMales();
        }
        return count;
    }

    public int getNumberFemales(){
        int count = 0;
        for (PlayerGroup group: playersGroups) {
            count += group.getNumberFemales();
        }
        return count;
    }

    public int getTotalScore(){
        int count = 0;
        for (PlayerGroup group: playersGroups) {
            count += group.getTotalScore();
        }
        return count;
    }

    public double getMultiplicativeScore(){
        double count = 1.0;
        for(PlayerGroup group: playersGroups){
            count *= group.getMultiplicativeScore();
        }
        return count;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SortedArrayList<PlayerGroup> getPlayersGroups() {
        return playersGroups;
    }

    public void setPlayersGroups(SortedArrayList<PlayerGroup> playersGroups) {
        this.playersGroups = playersGroups;
    }

    public int getDesiredNumberMales() {
        return desiredNumberMales;
    }

    public void setDesiredNumberMales(int desiredNumberMales) {
        this.desiredNumberMales = desiredNumberMales;
    }

    public int getDesiredNumberFemales() {
        return desiredNumberFemales;
    }

    public void setDesiredNumberFemales(int desiredNumberFemales) {
        this.desiredNumberFemales = desiredNumberFemales;
    }

    public int getDesiredTeamSize() {
        return desiredTeamSize;
    }

    public void setDesiredTeamSize(int desiredTeamSize) {
        this.desiredTeamSize = desiredTeamSize;
    }

}
