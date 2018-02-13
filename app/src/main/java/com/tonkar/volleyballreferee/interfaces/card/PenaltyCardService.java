package com.tonkar.volleyballreferee.interfaces.card;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.Set;

public interface PenaltyCardService extends BasePenaltyCardService {

    void addPenaltyCardListener(PenaltyCardListener listener);

    void removePenaltyCardListener(PenaltyCardListener listener);

    void givePenaltyCard(TeamType teamType, PenaltyCardType penaltyCardType, int number);

    Set<Integer> getExpulsedOrDisqualifiedPlayersForCurrentSet(TeamType teamType);
}
