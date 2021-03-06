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

            double lowestMultValue = Double.MAX_VALUE;
            for(Team team: teams){
                if(team.getMultiplicativeScore() < lowestMultValue){
                    lowestMultValue = team.getMultiplicativeScore();
                }
            }

            for(Team team: teams){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Team ").append(team.getName()).append("\n");
                stringBuilder.append(
                        String.format("Total Players: %s Males: %s Females %s",
                                team.getNumberPlayers(), team.getNumberMales(), team.getNumberFemales())).append("\n");
                stringBuilder.append("\tTotal Points: ").append(team.getTotalScore()).append("\n");
                stringBuilder.append("\tAverage Points Per Player: ").append(team.getAveragePointsPerPlayer()).append("\n");
                stringBuilder.append("\tMultiplicative Factor ").append(team.getMultiplicativeScore()/lowestMultValue).append("\n");
                for(PlayerGroup playerGroup: team.getPlayersGroups()){
                    for(Player player : playerGroup.getPlayers()){
                        stringBuilder.append(printPlayer(player));
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

    private String printPlayer(Player player){
        String playerOutput = "\t\t" + player.getName() + "," +
                player.getAggregateScore() + "," +
                player.getAthleticism() + "," +
                player.getThrowing() + "," +
                player.getExperience() + "," +
                player.getSex() + "," +
                player.getBaggageCode() + "\n";
        return playerOutput;
    }

}
