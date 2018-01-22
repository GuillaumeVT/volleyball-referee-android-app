package com.tonkar.volleyballreferee.business.data;

import android.content.Context;

import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SavedTeams implements SavedTeamsService {

    private final Context         mContext;
    private final List<SavedTeam> mSavedTeams;
    private       SavedTeam       mSavedTeam;

    public SavedTeams(Context context) {
        mContext = context;
        mSavedTeams = new ArrayList<>();
    }

    @Override
    public void loadSavedTeams() {
        mSavedTeam = null;
        mSavedTeams.clear();
        mSavedTeams.addAll(JsonDataReader.readSavedTeams(mContext, SAVED_TEAMS_FILE));
    }

    @Override
    public List<BaseIndoorTeamService> getSavedTeamServiceList() {
        List<BaseIndoorTeamService> list = new ArrayList<>();
        list.addAll(mSavedTeams);
        return list;
    }

    @Override
    public BaseIndoorTeamService getSavedTeamService(String teamName, GenderType genderType) {
        SavedTeam matching = null;

        for (SavedTeam savedTeam : mSavedTeams) {
            if (savedTeam.getTeamName(null).equals(teamName) && savedTeam.getGenderType().equals(genderType)) {
                matching = savedTeam;
            }
        }

        return matching;
    }

    @Override
    public void createTeam() {
        mSavedTeam = new SavedTeam(new IndoorTeamDefinition(TeamType.HOME));
    }

    @Override
    public void editTeam(String teamName, GenderType genderType) {
        mSavedTeam = (SavedTeam) getSavedTeamService(teamName, genderType);
        mSavedTeams.remove(mSavedTeam);
    }

    @Override
    public BaseIndoorTeamService getCurrentTeam() {
        return mSavedTeam;
    }

    @Override
    public void saveCurrentTeam() {
        mSavedTeams.add(mSavedTeam);
        JsonDataWriter.writeSavedTeams(mContext, SAVED_TEAMS_FILE, mSavedTeams);
        mSavedTeam = null;
    }

    @Override
    public void deleteSavedTeam(String teamName, GenderType genderType) {
        for (Iterator<SavedTeam> iterator = mSavedTeams.iterator(); iterator.hasNext();) {
            SavedTeam savedTeam = iterator.next();
            if (savedTeam.getTeamName(null).equals(teamName) && savedTeam.getGenderType().equals(genderType)) {
                iterator.remove();
            }
        }
        JsonDataWriter.writeSavedTeams(mContext, SAVED_TEAMS_FILE, mSavedTeams);
    }

    @Override
    public void deleteAllSavedTeams() {
        mSavedTeams.clear();
        JsonDataWriter.writeSavedTeams(mContext, SAVED_TEAMS_FILE, mSavedTeams);
    }

    @Override
    public void createAndSaveTeamFrom(BaseIndoorTeamService indoorTeamService, TeamType teamType) {
        if (getSavedTeamService(indoorTeamService.getTeamName(teamType), indoorTeamService.getGenderType(teamType)) == null) {
            createTeam();
            copyTeam(indoorTeamService, mSavedTeam, teamType);
            saveCurrentTeam();
        }
    }

    @Override
    public void copyTeam(BaseIndoorTeamService source, BaseIndoorTeamService dest, TeamType teamType) {
        dest.setTeamName(teamType, source.getTeamName(teamType));
        dest.setTeamColor(teamType, source.getTeamColor(teamType));
        dest.setLiberoColor(teamType, source.getLiberoColor(teamType));
        dest.setGenderType(teamType, source.getGenderType(teamType));

        for (Integer number: dest.getPlayers(teamType)) {
            dest.removePlayer(teamType, number);
        }
        for (Integer number: dest.getLiberos(teamType)) {
            dest.removeLibero(teamType, number);
        }

        for (Integer number: source.getPlayers(teamType)) {
            dest.addPlayer(teamType, number);
        }
        for (Integer number: source.getLiberos(teamType)) {
            dest.addLibero(teamType, number);
        }

        dest.setCaptain(teamType, source.getCaptain(teamType));
    }

}
