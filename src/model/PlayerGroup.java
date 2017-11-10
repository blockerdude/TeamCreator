package model;

import java.util.ArrayList;
import java.util.List;

import static sun.audio.AudioPlayer.player;

public class PlayerGroup {

    private ArrayList<Player> players;

    public PlayerGroup(ArrayList<Player> players) {
        this.players = players;
    }

    public PlayerGroup(Player player){
        this.players = new ArrayList<>();
        players.add(player);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player){
        this.players.add(player);
    }

    public int getTotalScore(){
        int score = 0;
        for (Player player: players) {
            score += player.getAggregateScore();
        }
        return score;
    }

    public double getMultiplicativeScore() {
        double score = 1.0;
        for(Player player : players){
            score *= player.getAggregateScore();
        }
        return score;
    }

    public int getPlayerCount(){
        return players.size();
    }

    public int getNumberMales(){
        return getGenderCount(Gender.male);
    }

    public int getNumberFemales(){
        return getGenderCount(Gender.female);
    }

    private int getGenderCount(Gender desiredGender){
        int count = 0;
        for (Player player: players) {
            if(player.getGender().equals(desiredGender)){
                count ++;
            }
        }
        return count;
    }

    public boolean containsPlayers(PlayerGroup other){
        for(Player thisPlayer : players){
            for(Player thatPlayer: other.players){
                if(thisPlayer.equals(thatPlayer)){
                    return true;
                }
            }
        }
        return false;
    }

}
