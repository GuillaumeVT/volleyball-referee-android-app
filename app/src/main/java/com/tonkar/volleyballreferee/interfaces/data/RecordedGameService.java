package com.tonkar.volleyballreferee.interfaces.data;

import com.tonkar.volleyballreferee.interfaces.BaseGeneralService;
import com.tonkar.volleyballreferee.interfaces.sanction.BaseSanctionService;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.score.BaseScoreService;
import com.tonkar.volleyballreferee.interfaces.BaseTimeService;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.BaseTimeoutService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;

import java.util.List;

public interface RecordedGameService extends BaseGeneralService, BaseScoreService, BaseTeamService, BaseTimeoutService, BaseSanctionService, BaseTimeService, WebGameService {

    boolean matchesFilter(String text);

    int getActingCaptain(TeamType teamType, int setIndex);

    List<Timeout> getTimeoutsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

    List<Substitution> getSubstitutionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

    List<Sanction> getSanctionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

}
