package model;

import java.util.ArrayList;

/**
 * A model that groups players that are intended to be grouped together.
 */
public class PlayerGroup {

    private ArrayList<Player> players;

    public PlayerGroup(ArrayList<Player> players) {
        this.players = players;
    }

    public PlayerGroup(Player player) {
        this.players = new ArrayList<>();
        players.add(player);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    /**
     * @return the raw aggregate score of all the players within the group.
     */
    public int getTotalScore() {
        int score = 0;
        for (Player player : players) {
            score += player.getAggregateScore();
        }
        return score;
    }

    /**
     * @return the aggregate score of all of the players within the group multiplied together
     */
    public double getMultiplicativeScore() {
        double score = 1.0;
        for (Player player : players) {
            score *= player.getAggregateScore();
        }
        return score;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getNumberMales() {
        return getSexCount(Sex.Male);
    }

    public int getNumberFemales() {
        return getSexCount(Sex.Female);
    }

    private int getSexCount(Sex desiredSex) {
        int count = 0;
        for (Player player : players) {
            if (player.getSex().equals(desiredSex)) {
                count++;
            }
        }
        return count;
    }

    public boolean containsPlayers(PlayerGroup other) {
        for (Player thisPlayer : players) {
            for (Player thatPlayer : other.players) {
                if (thisPlayer.equals(thatPlayer)) {
                    return true;
                }
            }
        }
        return false;
    }

}
