package runner;

import model.GamesMissing;
import model.Gender;
import model.Player;
import model.PlayerGroup;

import java.io.*;
import java.util.*;

import static model.Gender.female;
import static model.Gender.male;

public class Importer {

    private final int NAME = 0;
    private final int ATHLETIISM = 2;
    private final int THROWS = 3;
    private final int EXPERIENCE = 4;
    private final int GENDER = 5;
    private final int BAGGAGE_ID = 6;

    public List<PlayerGroup> getPlayersFromFile(String fileName){
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

        //add playerGroups from the map
        playerGroups.addAll(baggageMap.values());

        return playerGroups;

    }

    private Player createPlayerFromLine(String currentLine, int id) {
       String[] playerInformation = Arrays.asList(currentLine.split("\\s*,\\s*", -1)).toArray(new String[0]);
        //TODO: find a better fix. This adds the baggage Code when its unlisted
//        if(playerInformation.size() == 6){
//            playerInformation.add("");
//        }
        Gender gender = playerInformation[GENDER].toLowerCase().equals("female") ? female : male;

        return new Player(Integer.valueOf(playerInformation[ATHLETIISM]),
                                    Integer.valueOf(playerInformation[THROWS]),
                                    Integer.valueOf(playerInformation[EXPERIENCE]),
                                    id,
                                    playerInformation[NAME],
                                    playerInformation[BAGGAGE_ID],
                                    new ArrayList<String>(),
                                    GamesMissing.zeroToTwo,
                                    gender);
    }
}
