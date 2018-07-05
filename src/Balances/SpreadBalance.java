package Balances;

import model.Team;

public class SpreadBalance implements Balance {

    public SpreadBalance(){

    }

    @Override
    public boolean balance(Team one, Team two, double previousValue) {
        return true;
    }
}
