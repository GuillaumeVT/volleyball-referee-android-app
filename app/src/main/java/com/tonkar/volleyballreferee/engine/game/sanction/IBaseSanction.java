package com.tonkar.volleyballreferee.engine.game.sanction;

import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.List;

public interface IBaseSanction {

    List<ApiSanction> getGivenSanctions(TeamType teamType);

    List<ApiSanction> getGivenSanctions(TeamType teamType, int setIndex);

    List<ApiSanction> getSanctions(TeamType teamType, int number);

    boolean hasSanctions(TeamType teamType, int number);

}
