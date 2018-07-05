package Balances;

import model.Team;

public class AveragePointsBalance implements Balance {

    public AveragePointsBalance() {

    }

    @Override
    public boolean balance(Team one, Team two, double previousValue) {
        return true;
    }
}
