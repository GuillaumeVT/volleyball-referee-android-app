package com.tonkar.volleyballreferee.interfaces.sanction;

import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.List;

public interface BaseSanctionService {

    List<Sanction> getGivenSanctions(TeamType teamType);

    List<Sanction> getGivenSanctions(TeamType teamType, int setIndex);

    List<Sanction> getSanctions(TeamType teamType, int number);

    boolean hasSanctions(TeamType teamType, int number);

}
