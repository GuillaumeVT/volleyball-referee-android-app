package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.api.ApiSanction;
import com.tonkar.volleyballreferee.api.ApiSubstitution;
import com.tonkar.volleyballreferee.api.ApiTimeout;
import com.tonkar.volleyballreferee.interfaces.BaseGeneralService;
import com.tonkar.volleyballreferee.interfaces.sanction.BaseSanctionService;
import com.tonkar.volleyballreferee.interfaces.score.BaseScoreService;
import com.tonkar.volleyballreferee.interfaces.BaseTimeService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.BaseTimeoutService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;

import java.util.List;

public interface StoredGameService extends BaseGeneralService, BaseScoreService, BaseTeamService, BaseTimeoutService, BaseSanctionService, BaseTimeService {

    boolean matchesFilter(String text);

    int getActingCaptain(TeamType teamType, int setIndex);

    List<ApiTimeout> getTimeoutsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

    List<ApiSubstitution> getSubstitutionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

    List<ApiSanction> getSanctionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

}
