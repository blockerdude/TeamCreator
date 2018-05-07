package Rules;

import model.Team;

public class BalanceByTotalPointsRule implements Rule {

    public BalanceByTotalPointsRule() {

    }

    @Override
    public boolean validate(Team first, Team second) {
        return false;
    }
}
