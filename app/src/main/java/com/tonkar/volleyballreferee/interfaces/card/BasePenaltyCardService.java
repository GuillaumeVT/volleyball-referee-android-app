package com.tonkar.volleyballreferee.interfaces.card;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.List;

public interface BasePenaltyCardService {

    List<PenaltyCard> getGivenPenaltyCards(TeamType teamType);

    List<PenaltyCard> getGivenPenaltyCards(TeamType teamType, int setIndex);

    List<PenaltyCard> getPenaltyCards(TeamType teamType, int number);

    boolean hasPenaltyCards(TeamType teamType, int number);

}
