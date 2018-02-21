package com.tonkar.volleyballreferee.interfaces.sanction;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.Set;

public interface SanctionService extends BaseSanctionService {

    void addSanctionListener(SanctionListener listener);

    void removeSanctionListener(SanctionListener listener);

    void giveSanction(TeamType teamType, SanctionType sanctionType, int number);

    Set<Integer> getExpulsedOrDisqualifiedPlayersForCurrentSet(TeamType teamType);
}
