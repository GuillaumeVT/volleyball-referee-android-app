package com.tonkar.volleyballreferee.interfaces.card;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public interface PenaltyCardListener {

    void onPenaltyCard(TeamType teamType, PenaltyCardType penaltyCardType, int number);
}
