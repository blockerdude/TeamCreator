package runner;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {


    public static void main(String args[]){

        int numPlayersPerTeam = 16;
        int numMalesPerTeam = 9;
        int numFemalesPerTeam = 7;
        int numTeams = 18;
        int totalMales = numTeams * numMalesPerTeam;
        int totalFemales = numTeams * numFemalesPerTeam;
        int groupIndex = 0;


        //List<PlayerGroup> groups = new GeneratePlayers().generatePlayerGroups(totalMales, totalFemales, 85);

        Importer importer = new Importer();
        List<PlayerGroup> groups= importer.getPlayersFromFile("TR League Anonymous.csv");
        List<PlayerGroup> baggaged = new ArrayList<>();
        List<PlayerGroup> solos = new ArrayList<>();
        //TODO: adding two solo players to make even teams, just for testing
        //need to remove this and change the algorithm to account for not equal players
        Player testPlayer1 = new Player(4, 4, 4, 998, "testPlayer1", "", new ArrayList<String>(), GamesMissing.zeroToTwo, Gender.male);
        Player testPlayer2 = new Player(4, 4, 4, 998, "testPlayer2", "", new ArrayList<String>(), GamesMissing.zeroToTwo, Gender.female);
        groups.add(new PlayerGroup(testPlayer1));
        groups.add(new PlayerGroup(testPlayer2));


        int count = 0;
        int numMales = 0;
        int numFemales = 0;
        double totalScore = 0;
        for(PlayerGroup group: groups){
            count += group.getPlayerCount();
            numMales += group.getNumberMales();
            numFemales += group.getNumberFemales();
            totalScore += group.getTotalScore();
            if(group.getPlayerCount() > 1){
                baggaged.add(group);
            }
            else{
                solos.add(group);
            }
        }




        double targetTeamScore = totalScore/numTeams;

        List<Team> teams = new ArrayList<>();

        //create all the teams
        for(int i=0; i< numTeams; i++){
            Team team = new Team("Team_"+i, new SortedArrayList<PlayerGroup>(), numPlayersPerTeam, numMalesPerTeam, numFemalesPerTeam);
            teams.add(team);
        }


        //snake the baggage and solos into the teams
        while(baggaged.size() !=0){
            for(int i=0; i< numTeams; i ++) {
                addPlayerGroupsToTeam(baggaged, teams.get(i), true);
            }
        }

        while(solos.size() != 0) {
            for (int i = 0; i < teams.size(); i++) {
                addPlayerGroupsToTeam(solos, teams.get(i), false);
            }
        }

//        //Team gender composition
//        for(int x=0; x<teams.size(); x++){
//            System.out.println(teams.get(x).getName() + ": " + teams.get(x).getNumberMales() + " " + teams.get(x).getNumberFemales());
//        }





        //5) Tally up total for each team
        System.out.println("Target Team Score:" + targetTeamScore);

        teams = sortTeams_byPoints(teams);

        int currentDifference = teams.get(0).getTotalScore() - teams.get(teams.size()-1).getTotalScore();
        int previousDifference = 0;

        while(currentDifference != previousDifference || currentDifference >= 10) {
            System.out.println("Difference: " + currentDifference);
            for(int x=0; x<teams.size(); x++){
                System.out.println(teams.get(x).getName() + ": " + teams.get(x).getTotalScore() + " size: " + teams.get(x).getNumberPlayers());
            }
            for (int x = 0; x < teams.size() / 2; x++) {
                balanceTeams(teams.get(x), teams.get(teams.size() - x - 1));
            }
            teams = sortTeams_byPoints(teams);
            previousDifference = currentDifference;
            currentDifference = teams.get(0).getTotalScore() - teams.get(teams.size()-1).getTotalScore();
        }
        System.out.println("\n\n\n");

        System.out.println("Got a difference of " + currentDifference);


        //spread sort is working now, should be able to evaluate all teams



        for(int x=0; x<teams.size(); x++){
            System.out.println(teams.get(x).getName() + ": " + teams.get(x).getTotalScore() + " " + teams.get(x).getMultiplicativeScore() + " " + teams.get(x).getNumberMales() + " " + teams.get(x).getNumberFemales());
        }

        for(int x=0; x<numTeams-1; x+=2){
            dumbSwitch_spread(teams.get(x), teams.get(x+1));
            dumbSwitch_spread(teams.get(x), teams.get(x+1));
        }

        for(int x=0; x<teams.size(); x++){
            System.out.println(teams.get(x).getName() + ": " + teams.get(x).getTotalScore() + " " + teams.get(x).getMultiplicativeScore() + " " + teams.get(x).getNumberMales() + " " + teams.get(x).getNumberFemales());
        }



        //check to see if anyone player is double used
        //probably can be deleted, none have been duplicated so far
        Map<String, Integer> players= new HashMap<>();
        for(Team team : teams) {
            for (PlayerGroup playerGroup : team.getPlayersGroups()){
                for(Player player : playerGroup.getPlayers()) {
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

    private static void printTeam(Team team){
        for (PlayerGroup pg: team.getPlayersGroups()) {
            System.out.println(pg.getTotalScore());
        }
    }

    private static void addPlayerGroupsToTeam(List<PlayerGroup> groups, Team team, boolean baggage) {
        if(groups.size() != 0) {
            PlayerGroup curGroup = groups.get(0);

            int numMales = team.getNumberMales();
            int numFemales = team.getNumberFemales();


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

            if(baggage) {
                //failed to add the group in, can't fit, so remove a group from the current team, and keep trying.
                //Goal is to cycle the hard groups in and the easy ones out, hopefully it will work.
                groups.add(team.getPlayersGroups().remove(0));
                //recall this with the same team
                addPlayerGroupsToTeam(groups, team, baggage);
            }


        }

    }


    private static List<Team> sortTeams_byPoints(List<Team> teams) {
        ArrayList<Team> sortedList = new ArrayList<>();
        while(teams.size() != 0){
            int largest = 0;
            Team curTeam;
            int largestPosition = -1;
            for(int x=0; x<teams.size(); x++){
                curTeam = teams.get(x);
                if(curTeam.getTotalScore() > largest){
                    largest = curTeam.getTotalScore();
                    largestPosition = x;
                }
            }
            sortedList.add(teams.remove(largestPosition));
        }

        return sortedList;
    }

    private static void balanceTeams(Team high, Team low){
        int highScore = high.getTotalScore();
        int lowScore = low.getTotalScore();
        //knapsack(high, low);
        dumbSwitch_points(high, low);
    }

    public static void knapsack(Team high, Team low) {
//
//        System.out.println("Team 1 : " + high.getTotalScore() + " " + high.getNumberPlayers());
//        System.out.println("Team 2 : " + low.getTotalScore() + " " + low.getNumberPlayers());

        SortedArrayList<PlayerGroup> groups = new SortedArrayList<>(high.getPlayersGroups());
        groups.addAll(low.getPlayersGroups());


        int N = groups.size();   // number of items
        int W = high.getNumberPlayers();   // maximum weight of knapsack

        PlayerGroup[] score = new PlayerGroup[N+1];
        int[] weight = new int[N+1];

        //populate the list
        for (int n = 1; n <= N; n++) {
            score[n] = groups.get(n-1);
            weight[n] = groups.get(n-1).getPlayerCount();
        }

        // opt[n][w] = max profit of packing items 1..n with weight limit w
        // sol[n][w] = does opt solution to pack items 1..n with weight limit w include item n?
        int[][] opt1 = new int[N+1][W+1];
        boolean[][] sol1 = new boolean[N+1][W+1];

        int[][] opt2 = new int[N+1][W+1];
        boolean[][] sol2 = new boolean[N+1][W+1];

        boolean isOne = true;

        int[][] opt = opt1;
        boolean[][] sol = sol1;

        for (int n = 1; n <= N; n++) {
            for (int w = 1; w <= W; w++) {

//                if(isOne){
//                    opt = opt1;
//                    sol = sol1;
//                }
//                else{
//                    opt = opt2;
//                    sol = sol2;
//                }

                // don't take item n
                int option1 = opt[n-1][w];

                // take item n
                int option2 = Integer.MIN_VALUE;
                if (weight[n] <= w) option2 = score[n].getTotalScore() + opt[n-1][w-weight[n]];

                // select better of two options
                opt[n][w] = Math.max(option1, option2);
                sol[n][w] = (option2 > option1);

            }
        }

        // determine which items to take
        boolean[] take1 = new boolean[N+1];

        for (int n = N, w = W; n > 0; n--) {
            if (sol1[n][w]) {
                take1[n] = true;
                w = w - weight[n];
            }
            else {
                take1[n] = false;
            }
        }

        SortedArrayList<PlayerGroup> group1 = new SortedArrayList<>();
        SortedArrayList<PlayerGroup> group2 = new SortedArrayList<>();
        int team1Score = 0;
        int team2Score = 0;
        int team1Players = 0;
        int team2Players = 0;
        for(int n=1; n<=N; n++){
            if(take1[n]){
                team1Score += score[n].getTotalScore();
                team1Players ++;
                group1.add(score[n]);
            }
            else{
                team2Score += score[n].getTotalScore();
                team2Players ++;
                group2.add(score[n]);
            }
        }

        System.out.println("Team 1 : " + team1Score + " " + team1Players);
        System.out.println("Team 2 : " + team2Score + " " + team2Players);

        high.setPlayersGroups(group1);
        low.setPlayersGroups(group2);
    }

    private static void dumbSwitch_points(Team high, Team low){
        int targetDifference = high.getTotalScore() - low.getTotalScore();
        int maxDifference = targetDifference*2 -2;
        int minDifference = 2;
        int runningDifference = 0;
        ArrayList<SwapPair> swapList = new ArrayList<>();



        boolean[] pgInUse = new boolean[high.getNumberPlayers()];

        //ArrayList<boolean[]> pgInUse;
        //Skipping sorting for now, just find a swap that is less than max
        //'everything' is size one for now, trivial to divide it into groups

        PlayerGroup currentHighPlayerGroup, currentLowPlayerGroup;
        int rawDifference, calculatedDifference;
        for(int x=0; x<low.getPlayersGroups().size(); x++){
            currentLowPlayerGroup = low.getPlayersGroups().get(x);
            for(int y=0; y<high.getPlayersGroups().size(); y++){
                if(pgInUse[y]){
                    continue; //we are already using this element
                }
                currentHighPlayerGroup = high.getPlayersGroups().get(y);
                if(currentHighPlayerGroup.getPlayerCount() != currentLowPlayerGroup.getPlayerCount()){
                    continue; //different number of players between groups, don't mix them together
                }
                if(currentHighPlayerGroup.getNumberMales() != currentLowPlayerGroup.getNumberMales()){
                    continue; //different composition in gender ratio
                }
                if(currentHighPlayerGroup.getNumberFemales() != currentLowPlayerGroup.getNumberFemales()){
                    continue; //different composition in gender ratio
                }
                rawDifference = currentHighPlayerGroup.getTotalScore() - currentLowPlayerGroup.getTotalScore();
                calculatedDifference = rawDifference *2;
                if(calculatedDifference >= minDifference && calculatedDifference <= maxDifference){
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
            if(runningDifference >= targetDifference)
                break;
        }

        if(swapList.isEmpty()){
            return; //there are no swaps to do
        }
        int lastSwapValue = swapList.get(swapList.size()-1).getDifference();
        if(Math.abs(runningDifference - targetDifference) > Math.abs(runningDifference-lastSwapValue - targetDifference)){
            //taking out the last value is better
            swapList.remove(swapList.size()-1);
        }

        //next part, make the swaps
        for(SwapPair swapPair : swapList){
            low.removePlayerGroup(swapPair.getLow());
            high.addPlayerGroup(swapPair.getLow());

            low.addPlayerGroup(swapPair.getHigh());
            high.removePlayerGroup(swapPair.getHigh());
        }

    }

    private static void dumbSwitch_spread(Team one, Team two) {
        System.out.println("Doing " +one.getName() + " and " + two.getName());

        double spread = 1.0;

        spread = getSpread(one, two, spread);
        System.out.println("Spread before " + spread);


        List<List<List<PlayerGroup>>> globalList1 = new ArrayList<>();
        for(int x = 0; x<9; x++){
            globalList1.add(new ArrayList<List<PlayerGroup>>());
        }
        getAllGroupsOfEqualAndLesserSize(globalList1, one.getPlayersGroups(), 8);


        List<List<List<PlayerGroup>>> globalList2 = new ArrayList<>();
        for(int x = 0; x<9; x++){
            globalList2.add(new ArrayList<List<PlayerGroup>>());
        }
        getAllGroupsOfEqualAndLesserSize(globalList2, two.getPlayersGroups(), 8);

        List<PlayerGroup> optimalToSwitchFrom1 = null;
        List<PlayerGroup> optimalToSwitchFrom2 = null;
        double optimalSpread = spread;
        double currentSpread;

       // for(int x=2; x<globalList1.size(); x++){
        for(int x=2; x<6; x++){
            for(List<PlayerGroup> curPlayerGroup1: globalList1.get(x)){
                for(List<PlayerGroup> curPlayerGroup2: globalList2.get(x)){
                    //check that the cur pts/gender are the same
                    if(getTotalScoreForPlayerGroupList(curPlayerGroup1) != getTotalScoreForPlayerGroupList(curPlayerGroup2)){
                        continue;
                    }
                    if(getTotalFemalesForPlayerGroupList(curPlayerGroup1) != getTotalFemalesForPlayerGroupList(curPlayerGroup2)){
                        continue;
                    }
                    //TODO: need to determine which way is positive/negative, ie: which way to multiply and divide to see if things get better
                    currentSpread = calculateNewSpread(spread, curPlayerGroup1, curPlayerGroup2);
                    if(Math.abs(1-currentSpread) < Math.abs(1-optimalSpread)){
                        optimalSpread = currentSpread;
                        optimalToSwitchFrom1 = curPlayerGroup1;
                        optimalToSwitchFrom2 = curPlayerGroup2;
                    }
                }
            }
        }

        if(optimalToSwitchFrom1 == null){
            System.out.println("Spread after switching 0 players " + optimalSpread);
            return;
        }
        //TODO: change this to generate a list of playergroups
        int numSwitched = 0;
        for(PlayerGroup pg: optimalToSwitchFrom1){
            numSwitched += pg.getPlayerCount();
            one.removePlayerGroup(pg);
            two.addPlayerGroup(pg);
        }
        for(PlayerGroup pg: optimalToSwitchFrom2){
            one.addPlayerGroup(pg);
            two.removePlayerGroup(pg);
        }


        System.out.println("Spread after switching " + numSwitched + " players " + optimalSpread);

    }

    private static double getSpread(Team one, Team two, double spread) {
        List<Player> players1 = new ArrayList<>();
        List<Player> players2 = new ArrayList<>();

        for(PlayerGroup playerGroup : one.getPlayersGroups()){
            players1.addAll(playerGroup.getPlayers());
        }
        for(PlayerGroup playerGroup : two.getPlayersGroups()){
            players2.addAll(playerGroup.getPlayers());
        }

        for(int x = 0; x < players1.size(); x++){
            spread *= players1.get(x).getAggregateScore();
            spread /= players2.get(x).getAggregateScore();
        }
        return spread;
    }

    private static Object getTotalFemalesForPlayerGroupList(List<PlayerGroup> pgs) {
        int num = 0;
        for(PlayerGroup pg : pgs){
            num += pg.getNumberFemales();
        }
        return num;
    }

    private static int getTotalScoreForPlayerGroupList(List<PlayerGroup> pgs) {
        int score = 0;
        for(PlayerGroup pg : pgs){
            score += pg.getTotalScore();
        }
        return score;
    }

    private static double calculateNewSpread(double spread, List<PlayerGroup> curPlayerGroup1, List<PlayerGroup> curPlayerGroup2) {
        List<Player> group1Players = new ArrayList<>();
        List<Player> group2Players = new ArrayList<>();

        for(PlayerGroup pg: curPlayerGroup1){
            group1Players.addAll(pg.getPlayers());
        }

        for(PlayerGroup pg: curPlayerGroup2){
            group2Players.addAll(pg.getPlayers());
        }


        for(int x=0; x<group1Players.size(); x++){
            spread *= Math.pow(group1Players.get(x).getAggregateScore(), 2.0);
            spread /= Math.pow(group2Players.get(x).getAggregateScore(), 2.0);
        }
        return spread;
    }

    private static void getAllGroupsOfEqualAndLesserSize(List<List<List<PlayerGroup>>> globalList, List<PlayerGroup> playerGroups, int desiredSize){
        if(desiredSize == 1){
            for(PlayerGroup pg: playerGroups){
                if(pg.getPlayerCount() == 1){
                    ArrayList<PlayerGroup> pgToAdd = new ArrayList<>();
                    pgToAdd.add(pg);
                    globalList.get(desiredSize).add(pgToAdd);
                }
            }
            return;
        }
        else{
            getAllGroupsOfEqualAndLesserSize(globalList, playerGroups, desiredSize -1);
        }

        for(PlayerGroup pg : playerGroups){
            //TODO; combine below statements, have method that does combination do the zero check
            if(pg.getPlayerCount() == desiredSize){
                ArrayList<PlayerGroup> pgToAdd = new ArrayList<>();
                pgToAdd.add(pg);
                globalList.get(desiredSize).add(pgToAdd);
            }
            else{
                if(pg.getPlayerCount() < desiredSize){
                    combineSubSets(globalList, pg, desiredSize);
                }
            }
        }
    }

    private static void combineSubSets(List<List<List<PlayerGroup>>> globalList, PlayerGroup playerGroup, int desiredSize){
        int dif = desiredSize - playerGroup.getPlayerCount();
        List<PlayerGroup> combos = new ArrayList<>();
        //The -1 is to change between 0 and 1 based maths
        for(List<PlayerGroup> pgs : globalList.get(dif))
            doSomething(globalList, playerGroup, desiredSize, pgs);
    }

    private static void doSomething(List<List<List<PlayerGroup>>> globalList, PlayerGroup playerGroup, int desiredSize, List<PlayerGroup> pgs) {
//        for(PlayerGroup other : pgs){
//            if(other.containsPlayers(playerGroup)) {
//                //DO NOT CREATE A GROUP WITH THE SAME players MULTIPLE TIMES
//                continue;
//            }
//            else{
//                PlayerGroup newCombo = new PlayerGroup(playerGroup, other);
//                globalList.get(desiredSize).add(newCombo);
//            }
//        }

        if(pgs.contains(playerGroup)){
            return;
        }
        else{
            ArrayList<PlayerGroup> newCombo = new ArrayList<>(pgs);
            newCombo.add(playerGroup);
            globalList.get(desiredSize).add(newCombo);
        }
    }


}
