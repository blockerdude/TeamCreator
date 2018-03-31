package runner;

import Exceptions.DuplicatedPlayerException;
import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is currently used for testing out the algorithm which is still in development.
 * Nearly everything is based on the premise of an ultimate frisbee league with the number of teams and players
 * based on the available test data.
 */
public class Main {


    public static void main(String args[]) {
        validateUserArguments(args);
        String fileInput = args[0];
        String fileOutput = args[1];
        int numMales = Integer.parseInt(args[2]);
        int numFemales = Integer.parseInt(args[3]);
        int numTeams = Integer.parseInt(args[4]);
        normalTestRun(fileInput, fileOutput, numTeams, numMales, numFemales);
    }

    private static void validateUserArguments(String args[]) {
        if(args.length != 5){
            System.out.println("Please include five (5) input arguments");
            System.out.println("The order should be: File input name, File output name, Number males per team, " +
                    "number females per team, and finally the number of desired teams");
            System.out.println("\n\nAn example command might look like this");
            System.out.println("java -jar MUFA_Team_Creator.jar players.csv teams.txt 9 7 14");
            System.exit(0);
        }

    }

    private static void normalTestRun(String fileInput, String fileOutput, int numTeams, int numMalesPerTeam, int numFemalesPerTeam) {
        Importer importer = new Importer();
        List<PlayerGroup> baggaged = new ArrayList<>();
        List<PlayerGroup> solos = new ArrayList<>();
        List<PlayerGroup> groups = importer.getPlayerGroupsFromFile(fileInput);

        //Split the player groups into baggaged and solo groups for sorting later
        //TODO: update the distribution to not require different sized sorting
        for (PlayerGroup group : groups) {
            if (group.getPlayerCount() > 1) {
                baggaged.add(group);
            } else {
                solos.add(group);
            }
        }

        List<Team> teams = instantiateTeams(numMalesPerTeam, numFemalesPerTeam, numTeams, baggaged, solos);

        teams = balanceTeamsByAveragePointsPerPlayer(teams);
        teams = balanceTeamsBySpread(teams);

        checkForDuplicatedPlayer(teams);

        teams = orderTeamsByMultiplicativeScore(teams);
        Exporter exporter = new Exporter();
        exporter.printTeamsToFile(fileOutput, teams);
    }

    private static Team createTeamFromFile(String fileName, int malesPerTeam, int femalesPerTeam, String name) {
        Importer importer = new Importer();
        List<PlayerGroup> playerGroupsFromFile = importer.getPlayerGroupsFromFile(fileName);
        return new Team(name, new SortedPlayerGroupArrayList<>(playerGroupsFromFile), malesPerTeam + femalesPerTeam, malesPerTeam, femalesPerTeam);
    }

    private static List<Team> balanceTeamsByAveragePointsPerPlayer(List<Team> teams) {
        teams = orderTeamByAveragePointsPerPlayer(teams);
        double currentDifference = teams.get(0).getAveragePointsPerPlayer() - teams.get(teams.size() - 1).getAveragePointsPerPlayer();
        double previousDifference = 0;

        while (currentDifference != previousDifference || currentDifference >= 5) {
            for (int x = 0; x < teams.size() / 2; x++) {
                balanceTwoTeamsByAveragePoints(teams.get(x), teams.get(teams.size() - x - 1));
            }
            teams = orderTeamByAveragePointsPerPlayer(teams);
            previousDifference = currentDifference;
            currentDifference = teams.get(0).getAveragePointsPerPlayer() - teams.get(teams.size() - 1).getAveragePointsPerPlayer();
        }
        return teams;
    }

    private static List<Team> balanceTeamsBySpread(List<Team> teams) {
        teams = orderTeamsByMultiplicativeScore(teams);
        double currentDifference = getNormalizedMultiplicativeScore(teams.get(0))/ getNormalizedMultiplicativeScore(teams.get(teams.size() - 1));
        double previousDifference = -1;

        while (currentDifference != previousDifference) {
            for (int x = 0; x < teams.size() / 2; x += 2) {
                bruteForceBalanceSpread(teams.get(x), teams.get(teams.size() - x - 1));
            }
            teams = orderTeamsByMultiplicativeScore(teams);
            previousDifference = currentDifference;
            currentDifference = getNormalizedMultiplicativeScore(teams.get(0))/ getNormalizedMultiplicativeScore(teams.get(teams.size() - 1));
        }

        //We may have stopped at a local maximum,
//
//        previousDifference = -1;
//        while (currentDifference != previousDifference) {
//            for (int x = 0; x < teams.size() / 2; x += 2) {
//                bruteForceBalanceSpread(teams.get(x), teams.get(teams.size() - x - 1));
//            }
//            teams = orderTeamByAveragePointsPerPlayer(teams);
//            previousDifference = currentDifference;
//            currentDifference = teams.get(0).getMultiplicativeScore() / teams.get(teams.size() - 1).getMultiplicativeScore();
//        }

        return teams;
    }


    private static double getNormalizedMultiplicativeScore(Team team){
        double multiplicativeScore = team.getMultiplicativeScore();

        //update the multiplicative score based on the total number of players
        double averagePts = team.getAveragePointsPerPlayer();
        for (int y = 0; y < (team.getDesiredTeamSize() - team.getNumberPlayers()); y++) {
            multiplicativeScore *= averagePts;
        }
        return multiplicativeScore;
    }

    /**
     * The method checks to see if any players are being used in multiple teams,
     * currently only printing to console.
     *
     * @param teams The final teams
     */
    private static void checkForDuplicatedPlayer(List<Team> teams) {
        //check to see if anyone player is double used
        //probably can be deleted, none have been duplicated so far
        Map<String, Integer> players = new HashMap<>();
        for (Team team : teams) {
            for (PlayerGroup playerGroup : team.getPlayersGroups()) {
                for (Player player : playerGroup.getPlayers()) {
                    if (players.containsKey(player.getName())) {
                        //huge problem
                        throw new DuplicatedPlayerException(player.getName() + " has been duplicated");
                    } else {
                        players.put(player.getName(), player.getId());
                    }
                }
            }
        }
    }

    /**
     * This method will create the desired number of teams and put the correct number of males and females into each team
     * while still respecting the baggage requirements
     *
     * @param numMalesPerTeam   total number of males per team
     * @param numFemalesPerTeam total number of females per team
     * @param numTeams          total number of desired teams
     * @param baggaged          list of all playerGroups that have baggaged players
     * @param solos             list of all playerGroups that are solo players
     * @return a list of all the created teams
     */
    private static List<Team> instantiateTeams(int numMalesPerTeam, int numFemalesPerTeam, int numTeams, List<PlayerGroup> baggaged, List<PlayerGroup> solos) {

        int numPlayersPerTeam = numFemalesPerTeam + numMalesPerTeam;
        List<Team> teams = new ArrayList<>();
        //create all the teams
        for (int i = 0; i < numTeams; i++) {
            Team team = new Team("Team_" + i, new SortedPlayerGroupArrayList<PlayerGroup>(), numPlayersPerTeam, numMalesPerTeam, numFemalesPerTeam);
            teams.add(team);
        }

        //snake the baggage and solos into the teams
        while (baggaged.size() != 0) {
            for (Team team : teams) {
                addPlayerGroupToTeam(baggaged, team);
            }
        }

        while (solos.size() != 0) {
            for (Team team : teams) {
                addPlayerGroupToTeam(solos, team);
            }
        }

        return teams;
    }

    /**
     * Helper method for instantiateTeams, will actually place a playerGroup into the desired team
     * General process is to put the first available playerGroup in that doesn't break the given restraints
     * Will bump off existing playerGroups from the team if it was unable to find a fit for a baggaged group,
     * this helps prevent infinite loops, but should be removed for a better solution.
     *
     * @param groups the list of playerGroups to draw from
     * @param team   the team to add a playerGroup to
     */
    private static void addPlayerGroupToTeam(List<PlayerGroup> groups, Team team) {
        if (groups.size() != 0) {
            PlayerGroup curGroup = groups.get(0);
            boolean baggage = groups.get(0).getPlayerCount() > 1;

            //set flags for what genders are present
            boolean hasMales = curGroup.getNumberMales() > 0;
            boolean hasFemales = curGroup.getNumberFemales() > 0;
            boolean bothGenders = hasFemales && hasMales;
            boolean spaceForMales = team.getDesiredNumberMales() - team.getNumberMales() >= curGroup.getNumberMales();
            boolean spaceForFemales = team.getDesiredNumberFemales() - team.getNumberFemales() >= curGroup.getNumberFemales();

            if (bothGenders) {
                if (spaceForFemales && spaceForMales) {
                    team.addPlayerGroup(groups.remove(0));
                    return;
                }
            } else if (hasFemales) {
                if (spaceForFemales) {
                    team.addPlayerGroup(groups.remove(0));
                    return;
                }
            } else { //has males
                if (spaceForMales) {
                    team.addPlayerGroup(groups.remove(0));
                    return;
                }
            }

            if (baggage) {
                //failed to add the group in, can't fit, so remove a group from the current team, and keep trying.
                //Goal is to cycle the hard groups in and the easy ones out, hopefully it will work.
                groups.add(team.getPlayersGroups().remove(0));

                //recall this with the same team
                addPlayerGroupToTeam(groups, team);
            }

        }

    }

    /**
     * Order the given teams from highest to lowest based on the teams average points per player
     * Currently an O(N^2) algorithm that can be improved with any log(n) sort.
     *
     * @param teams the list of teams to sort
     * @return the sorted list of teams
     */
    private static List<Team> orderTeamByAveragePointsPerPlayer(List<Team> teams) {
        ArrayList<Team> sortedList = new ArrayList<>();
        while (teams.size() != 0) {
            double largestAveragePPP = 0.0;
            Team curTeam;
            int largestPosition = -1;
            for (int x = 0; x < teams.size(); x++) {
                curTeam = teams.get(x);
                double curTeamAveragePoints = curTeam.getAveragePointsPerPlayer();
                if (curTeamAveragePoints > largestAveragePPP) {
                    largestAveragePPP = curTeamAveragePoints;
                    largestPosition = x;
                }
            }
            sortedList.add(teams.remove(largestPosition));
        }

        return sortedList;
    }

    /**
     * Order the given teams from highest to lowest based on the teams multiplicative score
     *
     * @param teams The list of teams to sort
     * @return the sorted list of teams
     */
    private static List<Team> orderTeamsByMultiplicativeScore(List<Team> teams) {
        ArrayList<Team> sortedList = new ArrayList<>();
        while (teams.size() != 0) {
            double largest = 0;
            Team curTeam;
            int largestPosition = -1;
            for (int x = 0; x < teams.size(); x++) {
                curTeam = teams.get(x);
                double multiplicativeScore = getNormalizedMultiplicativeScore(curTeam);

                if (multiplicativeScore > largest) {
                    largest = multiplicativeScore;
                    largestPosition = x;
                }
            }
            sortedList.add(teams.remove(largestPosition));
        }

        return sortedList;
    }


    private static void balanceTwoTeamsByAveragePoints(Team high, Team low) {
        double targetDifference = (high.getAveragePointsPerPlayer() - low.getAveragePointsPerPlayer()) * 2;
        double maxDifference = calculateMaxDifferenceForAveragePointsBalance(high, low);
        int minDifference = 2;
        int runningDifference = 0;
        ArrayList<SwapPair> swapList = new ArrayList<>();


        boolean[] pgInUse = new boolean[high.getNumberPlayers()];
        //Skipping sorting for now, just find a swap that is less than max

        PlayerGroup currentHighPlayerGroup, currentLowPlayerGroup;
        int rawDifference, calculatedDifference;
        for (int x = 0; x < low.getPlayersGroups().size(); x++) {
            currentLowPlayerGroup = low.getPlayersGroups().get(x);
            for (int y = 0; y < high.getPlayersGroups().size(); y++) {
                if (pgInUse[y]) {
                    continue; //we are already using this element
                }
                currentHighPlayerGroup = high.getPlayersGroups().get(y);
                if (currentHighPlayerGroup.getPlayerCount() != currentLowPlayerGroup.getPlayerCount()) {
                    continue; //different number of players between groups, don't mix them together
                }
                if (currentHighPlayerGroup.getNumberMales() != currentLowPlayerGroup.getNumberMales()) {
                    continue; //different composition in sex ratio
                }
                if (currentHighPlayerGroup.getNumberFemales() != currentLowPlayerGroup.getNumberFemales()) {
                    continue; //different composition in sex ratio
                }
                rawDifference = currentHighPlayerGroup.getTotalScore() - currentLowPlayerGroup.getTotalScore();
                calculatedDifference = rawDifference * 2;
                if (calculatedDifference >= minDifference && calculatedDifference <= maxDifference) {
                    //add to swap map
                    swapList.add(new SwapPair(currentLowPlayerGroup, currentHighPlayerGroup, calculatedDifference));
                    //update in use list
                    pgInUse[y] = true;
                    //update the running value
                    runningDifference += calculatedDifference;

                    //for now quitting after one success, in the future will continue on better logic
                    break;
                }
            }
            if (runningDifference >= targetDifference)
                break;
        }

        if (swapList.isEmpty()) {
            return; //there are no swaps to do
        }
        int lastSwapValue = swapList.get(swapList.size() - 1).getDifference();
        if (swapList.size() != 1 && Math.abs(runningDifference - targetDifference) > Math.abs(runningDifference - lastSwapValue - targetDifference)) {
            //taking out the last value is better
            swapList.remove(swapList.size() - 1);
        }

        //next part, make the swaps
        for (SwapPair swapPair : swapList) {
            low.removePlayerGroup(swapPair.getLow());
            high.addPlayerGroup(swapPair.getLow());

            low.addPlayerGroup(swapPair.getHigh());
            high.removePlayerGroup(swapPair.getHigh());
        }
    }

    /**
     * Calculates the maximum difference in value that a high and low team can swap and get closer to
     * having more equal average points per player.
     *
     * @param high team with a greater average points per player
     * @param low  team with a lower average points per player
     * @return the maximum value that can be swapped to decrease the difference between high and low teams
     * <p>
     * TODO: The variables in this function could be renamed
     */
    private static double calculateMaxDifferenceForAveragePointsBalance(Team high, Team low) {
        double highTeamEval = high.getNumberPlayers() * low.getAveragePointsPerPlayer();
        double lowTeamEval = low.getNumberPlayers() * high.getAveragePointsPerPlayer();
        double highDifference = Math.abs(highTeamEval - high.getTotalScore());
        double lowDifference = Math.abs(lowTeamEval - low.getTotalScore());
        return Math.min(highDifference, lowDifference);
    }

    /**
     * Algorithm to balance the teams by the spread of their players points. Goal is to balance the given two teams
     * so both have similar distributions of points. ie: 10,10,20,20 = 15,15,15,15 in total points, but swapping so the
     * teams are both 10,15,15,20 is better.
     * <p>
     * This sort has the most work remaining, it is incredibly inefficient
     *
     * @param one the first team
     * @param two the second team
     */
    private static void bruteForceBalanceSpread(Team one, Team two) {
        double spread = getSpread(one, two);

        //TODO: make both of these dependent on their own team size, they don't have to be the same size
        int halfTeamSize = one.getNumberPlayers() / 2;

        List<List<List<PlayerGroup>>> globalList1 = new ArrayList<>();
        for (int x = 0; x <= halfTeamSize; x++) {
            globalList1.add(new ArrayList<List<PlayerGroup>>());
        }
        getAllGroupsOfEqualAndLesserSizeForATeam(globalList1, one.getPlayersGroups(), halfTeamSize);


        List<List<List<PlayerGroup>>> globalList2 = new ArrayList<>();
        for (int x = 0; x <= halfTeamSize; x++) {
            globalList2.add(new ArrayList<List<PlayerGroup>>());
        }
        getAllGroupsOfEqualAndLesserSizeForATeam(globalList2, two.getPlayersGroups(), halfTeamSize);

        List<PlayerGroup> optimalToSwitchFrom1 = null;
        List<PlayerGroup> optimalToSwitchFrom2 = null;
        double optimalSpread = spread;
        double currentSpread;

        //If we use the entire size, this algorithm can balloon to take several minutes for each call
        for (int x = 2; x < globalList1.size(); x++) {
            for (List<PlayerGroup> curPlayerGroup1 : globalList1.get(x)) {
                for (List<PlayerGroup> curPlayerGroup2 : globalList2.get(x)) {
                    //check that the cur pts/gender/total players  are the same
                    //Note: Even though we are balancing over Average points, because we hold #players constant we can still
                    //compare the total points of the two player groups.
                    if (getTotalScoreForPlayerGroupList(curPlayerGroup1) != getTotalScoreForPlayerGroupList(curPlayerGroup2)) {
                        continue;
                    }
                    if (getTotalFemalesForPlayerGroupList(curPlayerGroup1) != getTotalFemalesForPlayerGroupList(curPlayerGroup2)) {
                        continue;
                    }
                    if (getTotalMalesForPlayerGroupList(curPlayerGroup1) != getTotalMalesForPlayerGroupList(curPlayerGroup2)) {
                        continue;
                    }
                    currentSpread = updateSpreadCalculation(spread, curPlayerGroup1, curPlayerGroup2);
                    //Subtract 1 because we want to be as close to 1 as possible
                    if (Math.abs(1.0 - currentSpread) < Math.abs(1.0 - optimalSpread)) {
                        optimalSpread = currentSpread;
                        optimalToSwitchFrom1 = curPlayerGroup1;
                        optimalToSwitchFrom2 = curPlayerGroup2;
                    }
                }
            }
        }

        if (optimalToSwitchFrom1 == null) {
            return;
        }
        //TODO: change this to generate a list of playerGroups
        for (PlayerGroup pg : optimalToSwitchFrom1) {
            one.removePlayerGroup(pg);
            two.addPlayerGroup(pg);
        }
        for (PlayerGroup pg : optimalToSwitchFrom2) {
            one.addPlayerGroup(pg);
            two.removePlayerGroup(pg);
        }

    }

    /**
     * Helper method that calculates the spread between two teams. Spread is a measure of how skewed one team is from
     * another. Values tend to stick around 0.25 to ~3, though the range is technically 0 < x <= infinity. If the spread
     * is exactly 1, then the teams are perfectly balanced with regards to spread, if the value is less than 1, then team
     * one is more skewed, if the value is greater than 1 then team two is more skewed.
     * Being more skewed means that the players scores are more spread out,
     * for example: A team of 10, 10, 20, 20 has more skew than 12, 12, 18, 18
     * <p>
     * To deal with teams with different amount of players the team with fewer players will act like it has additional
     * players each with their average score. This should work for most cases when teams have very similar number of
     * players, but might need to be revisited in the future.
     *
     * @param one the first team
     * @param two the second team
     * @return the spread metric between the teams.
     */
    private static double getSpread(Team one, Team two) {
        List<Player> players1 = new ArrayList<>();
        List<Player> players2 = new ArrayList<>();
        List<Double> playerSpreadValues = new ArrayList<>();
        double spread = 1.0;

        for (PlayerGroup playerGroup : one.getPlayersGroups()) {
            players1.addAll(playerGroup.getPlayers());
        }
        for (PlayerGroup playerGroup : two.getPlayersGroups()) {
            players2.addAll(playerGroup.getPlayers());
        }

        for (Player player : players1) {
            playerSpreadValues.add(1.0 * player.getAggregateScore());
        }
        for (Player player : players2) {
            playerSpreadValues.add(1.0 / player.getAggregateScore());
        }

        //Add extra 'players' to the team that has fewer players.
        //TODO: make the following better....
        //There are more in team 1
        for (int x = 0; x < (players1.size() - players2.size()); x++) {
            playerSpreadValues.add(1.0 / two.getAveragePointsPerPlayer());
        }
        //There are more in team 2
        for (int x = 0; x < (players2.size() - players1.size()); x++) {
            playerSpreadValues.add(1.0 * one.getAveragePointsPerPlayer());
        }

        for (Double value : playerSpreadValues) {
            spread *= value;
        }

        return spread;
    }

    /*
     * Helper function for spread balance
     */
    private static Object getTotalFemalesForPlayerGroupList(List<PlayerGroup> pgs) {
        int num = 0;
        for (PlayerGroup pg : pgs) {
            num += pg.getNumberFemales();
        }
        return num;
    }

    /*
     * Helper function for spread balance
     */
    private static Object getTotalMalesForPlayerGroupList(List<PlayerGroup> pgs) {
        int num = 0;
        for (PlayerGroup pg : pgs) {
            num += pg.getNumberMales();
        }
        return num;
    }

    /*
     * Helper function for spread balance
     */
    private static int getTotalScoreForPlayerGroupList(List<PlayerGroup> pgs) {
        int score = 0;
        for (PlayerGroup pg : pgs) {
            score += pg.getTotalScore();
        }
        return score;
    }

    /**
     * This method will calculate what the spread will be if the two player groups were to swap teams
     * NOTE, the order in which the teams are passed in needs to remain in the same order that the initial spread was
     * calculated with.
     *
     * @param startingSpread  what the current spread between two teams are
     * @param curPlayerGroup1 the players team 1 will trade
     * @param curPlayerGroup2 the players team 2 will trade
     * @return the new spread value the two teams will have between them
     */
    private static double updateSpreadCalculation(double startingSpread, List<PlayerGroup> curPlayerGroup1, List<PlayerGroup> curPlayerGroup2) {
        List<Player> group1Players = new ArrayList<>();
        List<Player> group2Players = new ArrayList<>();

        for (PlayerGroup pg : curPlayerGroup1) {
            group1Players.addAll(pg.getPlayers());
        }

        for (PlayerGroup pg : curPlayerGroup2) {
            group2Players.addAll(pg.getPlayers());
        }


        for (int x = 0; x < group1Players.size(); x++) {
            startingSpread *= Math.pow(group2Players.get(x).getAggregateScore(), 2.0);
            startingSpread /= Math.pow(group1Players.get(x).getAggregateScore(), 2.0);
        }
        return startingSpread;
    }

    /**
     * Helper function for spread balance that populates a global list of all possible player groups that can be traded for a specific team
     * TODO: Pass the team in instead of its list of PlayerGroups
     *
     * @param globalList   List<List<List<PlayerGroup>>> The outer list is the amount of players that can be traded,
     *                     index 0 is how to trade 0 players, 1 is how to trade 1 player, etc etc.
     *                     The next list is the list of all possible ways to trade for that amount of players.
     *                     The inner most list is the specific way to trade, ie: use these player
     *                     groups to trade for that many players.
     *                     For example: globalList.get(3).get(0) will return the first possible list of playerGroups that
     *                     can be traded that contain a total of 3 players for a given team.
     * @param playerGroups the playerGroups comprising a team
     * @param desiredSize  the desired range to generate for. The globalList will create all possible ways to trade x
     *                     number of players where x is up to desiredSize. Ie: if desiredSize = 6, we will also generate
     *                     how to trade 1, 2, 3, 4 and 5 players.
     */
    private static void getAllGroupsOfEqualAndLesserSizeForATeam(List<List<List<PlayerGroup>>> globalList, List<PlayerGroup> playerGroups, int desiredSize) {
        if (desiredSize == 1) {
            for (PlayerGroup pg : playerGroups) {
                if (pg.getPlayerCount() == 1) {
                    ArrayList<PlayerGroup> pgToAdd = new ArrayList<>();
                    pgToAdd.add(pg);
                    globalList.get(desiredSize).add(pgToAdd);
                }
            }
            return;
        } else {
            getAllGroupsOfEqualAndLesserSizeForATeam(globalList, playerGroups, desiredSize - 1);
        }

        for (PlayerGroup pg : playerGroups) {
            //TODO; combine above and below statements, have method that does combination do the zero check
            if (pg.getPlayerCount() == desiredSize) {
                ArrayList<PlayerGroup> pgToAdd = new ArrayList<>();
                pgToAdd.add(pg);
                globalList.get(desiredSize).add(pgToAdd);
            } else {
                if (pg.getPlayerCount() < desiredSize) {
                    combineSubSets(globalList, pg, desiredSize);
                }
            }
        }
    }

    /**
     * Combine a single playerGroup with every possible combination to create a new playerGroup that has the desired
     * number of players.
     * Overall strategy: Calculate the difference between the desired size and the amount of players the given group
     * has. Now we know what size of groups we need to combine with. Then loop through all of those lists and see if we
     * can combine the given playerGroup with that list. If we can, add it to the global list, otherwise don't.
     *
     * @param globalList  the list to keep track of all possible combinations for all possible sizes
     * @param playerGroup the playerGroup to combine with other Lists of playerGroups
     * @param desiredSize the amount of players the new list should contain
     */
    private static void combineSubSets(List<List<List<PlayerGroup>>> globalList, PlayerGroup playerGroup, int desiredSize) {
        int dif = desiredSize - playerGroup.getPlayerCount();
        for (List<PlayerGroup> pgs : globalList.get(dif)) {
            if (pgs.contains(playerGroup)) {
                //Do not include a player into a group that already contains that player
                return;
            } else {
                ArrayList<PlayerGroup> newCombo = new ArrayList<>(pgs);
                newCombo.add(playerGroup);
                globalList.get(desiredSize).add(newCombo);
            }
        }
    }

}
