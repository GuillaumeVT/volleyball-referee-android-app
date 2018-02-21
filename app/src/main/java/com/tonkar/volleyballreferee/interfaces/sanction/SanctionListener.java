package com.tonkar.volleyballreferee.interfaces.sanction;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public interface SanctionListener {

    void onSanction(TeamType teamType, SanctionType sanctionType, int number);
}
