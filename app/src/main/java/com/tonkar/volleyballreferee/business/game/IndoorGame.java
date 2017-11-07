package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.Team;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.business.team.IndoorTeam;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class IndoorGame extends Game {

    IndoorGame(final Rules rules) {
        super(GameType.INDOOR, rules);
    }

    @Override
    protected Team createTeam(TeamType teamType) {
        return new IndoorTeam(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetComplete()) {
            final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

            // In indoor volley, the teams change sides after the 8th during the tie break
            if (isTieBreakSet() && leadingScore == 8) {
                swapTeams(ActionOriginType.APPLICATION);
            }

            // In indoor volley, there are two technical timeouts at 8 and 16 but not during tie break
            if (getRules().areTechnicalTimeoutsEnabled()
                    && !isTieBreakSet()
                    && currentSet().getLeadingTeam().equals(teamType)
                    && (leadingScore == 8 || leadingScore == 16)
                    && currentSet().getPoints(TeamType.HOME) != currentSet().getPoints(TeamType.GUEST)) {
                notifyTechnicalTimeoutReached();
            }
        }
    }

    @Override
    public void removePoint(final TeamType teamType) {
        super.removePoint(teamType);

        final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

        // In indoor volley, the teams change sides after the 8th during the tie break
        if (isTieBreakSet() && leadingScore == 7) {
            swapTeams(ActionOriginType.APPLICATION);
        }
    }

}
