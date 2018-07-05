package Balances;

import model.Team;

public interface Balance {

    boolean balance(Team one, Team two, double previousValue);
}
