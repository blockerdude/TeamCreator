package runner;

import Exceptions.PlayerCreationException;
import model.GamesMissing;
import model.Sex;
import model.Player;
import model.PlayerGroup;

import java.io.*;
import java.util.*;

import static model.Sex.female;
import static model.Sex.male;


/**
 * This class parses a .csv file and reads each line as a Player object.
 *
 * @Author Nikhil Jain
 */
public class Importer {

    private final int NAME = 0;
    private final int ATHLETIISM = 2;
    private final int THROWS = 3;
    private final int EXPERIENCE = 4;
    private final int GENDER = 5;
    private final int BAGGAGE_ID = 6;

    /**
     * Reads in a csv file and attempts to return a list of all valid player groups within the file.
     *
     * @param fileName The name of the .csv file to be read in.
     * @return a list of all player groups within the file
     */
    public List<PlayerGroup> getPlayerGroupsFromFile(String fileName){
        File playerFile = new File(fileName);
        List<Player> players = new ArrayList<>();
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(playerFile));
            String currentLine;
            int lineNumber = 0;
            while((currentLine = fileReader.readLine()) != null){
                Player player = createPlayerFromLine(currentLine, lineNumber);
                players.add(player);
                lineNumber++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return combinePlayersIntoGroups(players);
    }


    private List<PlayerGroup> combinePlayersIntoGroups(List<Player> players) {
        List<PlayerGroup> playerGroups = new ArrayList<>();
        Map<String, PlayerGroup> baggageMap = new HashMap<>();

        for (Player player: players) {
            String baggageCode = player.getBaggageCode();
            if(baggageCode.equals("")){
                playerGroups.add(new PlayerGroup(player));
            }
            else{
                if(baggageMap.containsKey(baggageCode)){
                    baggageMap.get(baggageCode).addPlayer(player);
                }
                else{
                    baggageMap.put(baggageCode, new PlayerGroup(player));
                }
            }
        }

        playerGroups.addAll(baggageMap.values());

        return playerGroups;

    }

    private Player createPlayerFromLine(String currentLine, int id) {
       String[] playerInformation = Arrays.asList(currentLine.split("\\s*,\\s*", -1)).toArray(new String[0]);
       Sex sex = playerInformation[GENDER].toLowerCase().equals("female") ? female : male;

       Player player;
        try{
            player = new Player(Integer.valueOf(playerInformation[ATHLETIISM]),
                    Integer.valueOf(playerInformation[THROWS]),
                    Integer.valueOf(playerInformation[EXPERIENCE]),
                    id,
                    playerInformation[NAME],
                    playerInformation[BAGGAGE_ID],
                    GamesMissing.zeroToTwo,
                    sex);
        }catch(Exception e){
            throw new PlayerCreationException(String.format("Attempted to create a player with improper data %s", (Object[]) playerInformation));
        }

        return player;
    }
}
