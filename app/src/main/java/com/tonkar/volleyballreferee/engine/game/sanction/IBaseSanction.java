package com.tonkar.volleyballreferee.engine.game.sanction;

import com.tonkar.volleyballreferee.engine.api.model.ApiSanction;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.List;

public interface IBaseSanction {

    List<ApiSanction> getAllSanctions(TeamType teamType);

    List<ApiSanction> getAllSanctions(TeamType teamType, int setIndex);

    List<ApiSanction> getPlayerSanctions(TeamType teamType, int number);

    boolean hasSanctions(TeamType teamType, int number);

}
