package com.tonkar.volleyballreferee.engine.game.sanction;

import com.tonkar.volleyballreferee.engine.api.model.SanctionDto;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.List;

public interface IBaseSanction {

    List<SanctionDto> getAllSanctions(TeamType teamType);

    List<SanctionDto> getAllSanctions(TeamType teamType, int setIndex);

    List<SanctionDto> getPlayerSanctions(TeamType teamType, int number);

    boolean hasSanctions(TeamType teamType, int number);

}
