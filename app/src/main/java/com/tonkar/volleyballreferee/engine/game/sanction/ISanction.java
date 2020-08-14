package com.tonkar.volleyballreferee.engine.game.sanction;

import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.Set;

public interface ISanction extends IBaseSanction {

    void addSanctionListener(SanctionListener listener);

    void removeSanctionListener(SanctionListener listener);

    void giveSanction(TeamType teamType, SanctionType sanctionType, int number);

    Set<Integer> getEvictedPlayersForCurrentSet(TeamType teamType, boolean withExpulsions, boolean withDisqualifications);

    SanctionType getMostSeriousSanction(TeamType teamType, int number);

    SanctionType getPossibleDelaySanction(TeamType teamType);

    Set<SanctionType> getPossibleMisconductSanctions(TeamType teamType, int number);
}
