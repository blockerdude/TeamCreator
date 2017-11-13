package runner;

import model.Player;
import model.PlayerGroup;
import model.Team;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Exporter {

    public void printTeamsToFile(String fileName, List<Team> teams){
        try {
            PrintWriter fileWriter = new PrintWriter(fileName, "UTF-8");

            for(Team team: teams){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Team ").append(team.getName()).append("\n");
                stringBuilder.append("\tTotal Points: ").append(team.getTotalScore()).append("\n");
                stringBuilder.append("\tMultiplicative Points: ").append(team.getMultiplicativeScore()).append("\n");
                for(PlayerGroup playerGroup: team.getPlayersGroups()){
                    for(Player player : playerGroup.getPlayers()){
                        stringBuilder.append("\t\t").append(player.getName());
                        stringBuilder.append(" : ").append(player.getAggregateScore()).append("\n");
                    }
                }
                fileWriter.println(stringBuilder.toString());
                fileWriter.flush();
            }

            fileWriter.close();

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
