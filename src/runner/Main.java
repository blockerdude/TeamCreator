package runner;

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
        Importer importer = new Importer();
        List<PlayerGroup> baggaged = new ArrayList<>();
        List<PlayerGroup> solos = new ArrayList<>();
//        int numMalesPerTeam = 9;
//        int numFemalesPerTeam = 7;
//        int numTeams = 18;
        int numMalesPerTeam = 4;
        int numFemalesPerTeam = 0;
        int numTeams = 2;

        //List<PlayerGroup> groups = new GeneratePlayers().generatePlayerGroups(totalMales, totalFemales, 85);
        //List<PlayerGroup> groups= importer.getPlayerGroupsFromFile("TR League Anonymous.csv");
        //Adding two solo players to allow for all teams to have the same number of players.
        //TODO: update the logic to allow for uneven sized teams
        //Player testPlayer1 = new Player(4, 4, 4, 998, "testPlayer1", "", GamesMissing.zeroToTwo, Sex.male);
        //Player testPlayer2 = new Player(4, 4, 4, 998, "testPlayer2", "", GamesMissing.zeroToTwo, Sex.female);
        //groups.add(new PlayerGroup(testPlayer1));
        //groups.add(new PlayerGroup(testPlayer2));

        List<PlayerGroup> groups = importer.getPlayerGroupsFromFile("testGroup.csv");


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
        teams = balanceTeamsByTotalPoints(teams);


        //Spread sort is mostly working, with some problems, only attempt to sort by spread once for now.
        for (int x = 0; x < numTeams - 1; x += 2) {
            bruteForceBalanceSpread(teams.get(x), teams.get(x + 1));
        }

        checkForDuplicatedPlayer(teams);

        //TODO: print/dump the teams to a console/file

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
                        System.out.println(player.getName() + " is duplicated");
                    } else {
                        players.put(player.getName(), player.getId());
                    }
                }
            }
        }
    }

    /**
     * This algorithm will take in a list of teams and get all of the teams to be have as similar as possible total team
     * score, which is the summation of all of the players skills (athleticism, throwing, experience)
     *
     * @param teams the current teams, currently required to have the same number of players, and an even number of teams
     * @return a new list of teams in which every team is as equal as possible with all of the others
     */
    private static List<Team> balanceTeamsByTotalPoints(List<Team> teams) {
        int currentDifference = teams.get(0).getTotalScore() - teams.get(teams.size() - 1).getTotalScore();
        int previousDifference = 0;

        while (currentDifference != previousDifference || currentDifference >= 10) {
            for (int x = 0; x < teams.size() / 2; x++) {
                balanceTeams(teams.get(x), teams.get(teams.size() - x - 1));
            }
            teams = orderTeamByTotalPoints(teams);
            previousDifference = currentDifference;
            currentDifference = teams.get(0).getTotalScore() - teams.get(teams.size() - 1).getTotalScore();
        }
        return teams;
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
     * Order the given teams from highest to lowest based on the teams aggregate score
     *
     * @param teams The list of teams to sort
     * @return the sorted list of teams
     */
    private static List<Team> orderTeamByTotalPoints(List<Team> teams) {
        ArrayList<Team> sortedList = new ArrayList<>();
        while (teams.size() != 0) {
            int largest = 0;
            Team curTeam;
            int largestPosition = -1;
            for (int x = 0; x < teams.size(); x++) {
                curTeam = teams.get(x);
                if (curTeam.getTotalScore() > largest) {
                    largest = curTeam.getTotalScore();
                    largestPosition = x;
                }
            }
            sortedList.add(teams.remove(largestPosition));
        }

        return sortedList;
    }

    /**
     * This is the actual process to even out two given teams
     *
     * @param high the team with more points
     * @param low  the team with fewer points
     */
    private static void balanceTeams(Team high, Team low) {

        int targetDifference = high.getTotalScore() - low.getTotalScore();
        int maxDifference = targetDifference * 2 - 2;
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
        if (Math.abs(runningDifference - targetDifference) > Math.abs(runningDifference - lastSwapValue - targetDifference)) {
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
        System.out.println("Doing " + one.getName() + " and " + two.getName());

        double spread = getSpread(one, two);
        System.out.println("Spread before " + spread);

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
                    //check that the cur pts/gender are the same
                    if (getTotalScoreForPlayerGroupList(curPlayerGroup1) != getTotalScoreForPlayerGroupList(curPlayerGroup2)) {
                        continue;
                    }
                    if (getTotalFemalesForPlayerGroupList(curPlayerGroup1) != getTotalFemalesForPlayerGroupList(curPlayerGroup2)) {
                        continue;
                    }
                    //TODO: need to determine which way is positive/negative, ie: which way to multiply and divide to see if things get better
                    currentSpread = updateSpreadCalculation(spread, curPlayerGroup1, curPlayerGroup2);
                    if (Math.abs(1 - currentSpread) < Math.abs(1 - optimalSpread)) {
                        optimalSpread = currentSpread;
                        optimalToSwitchFrom1 = curPlayerGroup1;
                        optimalToSwitchFrom2 = curPlayerGroup2;
                    }
                }
            }
        }

        if (optimalToSwitchFrom1 == null) {
            System.out.println("Spread after switching 0 players " + optimalSpread);
            return;
        }
        //TODO: change this to generate a list of playerGroups
        int numSwitched = 0;
        for (PlayerGroup pg : optimalToSwitchFrom1) {
            numSwitched += pg.getPlayerCount();
            one.removePlayerGroup(pg);
            two.addPlayerGroup(pg);
        }
        for (PlayerGroup pg : optimalToSwitchFrom2) {
            one.addPlayerGroup(pg);
            two.removePlayerGroup(pg);
        }


        System.out.println("Spread after switching " + numSwitched + " players " + optimalSpread);

    }

    /**
     * Helper method that calculates the spread between two teams. Spread is a measure of how skewed one team is from
     * another. Values tend to stick around 0.25 to ~3, though the range is technically 0 < x <= infinity. If the spread
     * is exactly 1, then the teams are perfectly balanced with regards to spread, if the value is less than 1, then team
     * one is more skewed, if the value is greater than 1 then team two is more skewed.
     * Being more skewed means that the players scores are more spread out,
     * for example: A team of 10, 10, 20, 20 has more skew than 12, 12, 18, 18
     *
     * @param one the first team
     * @param two the second team
     * @return the spread metric between the teams.
     */
    private static double getSpread(Team one, Team two) {
        List<Player> players1 = new ArrayList<>();
        List<Player> players2 = new ArrayList<>();
        double spread = 1.0;

        for (PlayerGroup playerGroup : one.getPlayersGroups()) {
            players1.addAll(playerGroup.getPlayers());
        }
        for (PlayerGroup playerGroup : two.getPlayersGroups()) {
            players2.addAll(playerGroup.getPlayers());
        }

        for (int x = 0; x < players1.size(); x++) {
            spread *= players1.get(x).getAggregateScore();
            spread /= players2.get(x).getAggregateScore();
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
