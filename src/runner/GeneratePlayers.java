package runner;

import model.GamesMissing;
import model.Sex;
import model.Player;
import model.PlayerGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class used to generate fake players, is no longer currently in use.
 */
public class GeneratePlayers {

    private Random randomNumberGenerator = new Random();
    private final int LOW_SCORE = 1;
    private final int HIGH_SCORE = 7;
    private int id = 100;
    private int numMalesLeft;
    private int numFemalesLeft;
    private double percentMales;


    public List<PlayerGroup> generatePlayerGroups(int numMales, int numFemales, int percentBaggaged) {

        this.numFemalesLeft = numFemales;
        this.numMalesLeft = numMales;
        int numPlayers = numFemales + numMales;
        this.percentMales = (double)(100 * numMales)/numPlayers;
        List<PlayerGroup> playerGroups = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            PlayerGroup currentPlayerGroup;
            //roll the die to see if we create a solo player or a group
            int roll = generateRandomInt(0, 100); //TODO: move in line
            if(roll <= percentBaggaged && (numPlayers-i >= 2)){ //create baggage
              currentPlayerGroup = generatePlayerGroup(2);
               i++; //addl increment for the 2nd player
            }
            else{ //create solo player
            currentPlayerGroup = generatePlayerGroup(1);
            }
            playerGroups.add(currentPlayerGroup);

        }

        return playerGroups;
    }

    private int generateRandomInt(int low, int high) {
        return randomNumberGenerator.nextInt(high - low + 1) + low; //+1 for exclusive
    }

    private PlayerGroup generatePlayerGroup(int numPlayers) {
        ArrayList<Player> players = new ArrayList<>();
        if (numPlayers == 1) {
            players.add(generateRandomPlayer());
            return new PlayerGroup(players);
        }
        Player one = generateRandomPlayer();
        Player two = generateRandomPlayer();
        players.add(one);
        players.add(two);
        return new PlayerGroup(players);
    }

    private Player generateRandomPlayer() {
        int id = getId();
        Sex sex = getGender();
        Player player = new Player(
                generateRandomInt(LOW_SCORE, HIGH_SCORE),
                generateRandomInt(LOW_SCORE, HIGH_SCORE),
                generateRandomInt(LOW_SCORE, HIGH_SCORE),
                id,
                "Player_" + id,
                "",
                GamesMissing.values()[generateRandomInt(0, 6)],
                sex);

        return player;
    }

    private int getId() {
        return ++id;
    }

    private Sex getGender() {
        Sex sex = Sex.female;
        if (generateRandomInt(0, 100) <= percentMales) {
            sex = Sex.male;
        }
        if(sex == Sex.male){
            if(numMalesLeft > 0){
                numMalesLeft --;
                return Sex.male;
            }
            else{
                numFemalesLeft --;
                return Sex.female;
            }
        }
        else{
            if(numFemalesLeft > 0){
                numFemalesLeft --;
                return Sex.female;
            }
            else{
                numMalesLeft --;
                return Sex.male;
            }
        }
    }
}
