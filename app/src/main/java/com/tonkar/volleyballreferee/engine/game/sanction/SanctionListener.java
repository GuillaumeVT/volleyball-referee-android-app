package com.tonkar.volleyballreferee.engine.game.sanction;

import com.tonkar.volleyballreferee.engine.team.TeamType;

public interface SanctionListener {

    void onSanction(TeamType teamType, SanctionType sanctionType, int number);

    void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number);
}
