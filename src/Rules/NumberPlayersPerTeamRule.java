package Rules;

import model.Team;

/**
 * Ensure that both teams have the requisite number of required players,
 *
 */
public class NumberPlayersPerTeamRule implements Rule{

    private int minNumberOfMales;
    private int maxNumberOfMales;
    private int minNumberOfFemales;
    private int maxNumberOfFemales;


    public NumberPlayersPerTeamRule(int minNumberOfMales, int maxNumberOfMales, int minNumberOfFemales, int maxNumberOfFemales) {
        this.minNumberOfMales = minNumberOfMales;
        this.maxNumberOfMales = maxNumberOfMales;
        this.minNumberOfFemales = minNumberOfFemales;
        this.maxNumberOfFemales = maxNumberOfFemales;
    }

    @Override
    public boolean validate(Team first) {
        return (first.getNumberMales() >= minNumberOfMales && first.getNumberMales() <= maxNumberOfMales) &&
        (first.getNumberFemales() >= minNumberOfFemales && first.getNumberFemales() <= maxNumberOfFemales);
    }
}
