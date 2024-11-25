package com.tonkar.volleyballreferee.engine.service;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.IBaseSanction;
import com.tonkar.volleyballreferee.engine.game.score.IBaseScore;
import com.tonkar.volleyballreferee.engine.game.timeout.IBaseTimeout;
import com.tonkar.volleyballreferee.engine.team.*;

import java.util.List;

public interface IStoredGame extends IBaseGeneral, IBaseScore, IBaseTeam, IBaseTimeout, IBaseSanction, IBaseTime {

    int getGameCaptain(TeamType teamType, int setIndex);

    List<TimeoutDto> getTimeoutsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

    List<SubstitutionDto> getSubstitutionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

    List<SanctionDto> getSanctionsIfExist(TeamType teamType, int setIndex, int hPoints, int gPoints);

}
