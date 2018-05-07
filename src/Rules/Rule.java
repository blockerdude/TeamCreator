package Rules;

import model.Team;

public interface Rule {

    public boolean validate(Team first, Team second);
}
