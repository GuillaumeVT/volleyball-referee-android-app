package com.tonkar.volleyballreferee.interfaces.sanction;

import com.tonkar.volleyballreferee.api.ApiSanction;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.List;

public interface BaseSanctionService {

    List<ApiSanction> getGivenSanctions(TeamType teamType);

    List<ApiSanction> getGivenSanctions(TeamType teamType, int setIndex);

    List<ApiSanction> getSanctions(TeamType teamType, int number);

    boolean hasSanctions(TeamType teamType, int number);

}
