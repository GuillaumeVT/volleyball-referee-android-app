package com.tonkar.volleyballreferee.business.team;

import com.tonkar.volleyballreferee.interfaces.TeamType;

public class IndoorTeam extends Team {

    public IndoorTeam(final TeamType teamType) {
        super(teamType);
    }

    @Override
    protected Player createPlayer(int number) {
        return new IndoorPlayer(number);
    }

}
