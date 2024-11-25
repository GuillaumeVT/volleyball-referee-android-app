package com.tonkar.volleyballreferee.engine.database;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.database.model.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.GenderType;

import java.util.*;

public class VbrRepository {

    private static final String sCurrentGame = "current";
    private static final String sSetupGame   = "setup";

    private final FriendDao   mFriendDao;
    private final FullGameDao mFullGameDao;
    private final GameDao     mGameDao;
    private final LeagueDao   mLeagueDao;
    private final RulesDao    mRulesDao;
    private final TeamDao     mTeamDao;

    public VbrRepository(Context context) {
        VbrDatabase db = VbrDatabase.getInstance(context);
        mFriendDao = db.friendDao();
        mFullGameDao = db.fullGameDao();
        mGameDao = db.gameDao();
        mLeagueDao = db.leagueDao();
        mRulesDao = db.rulesDao();
        mTeamDao = db.teamDao();
    }

    public void insertFriend(final String friendId, final String friendPseudo, boolean syncInsertion) {
        Runnable runnable = () -> {
            FriendEntity friendEntity = new FriendEntity();
            friendEntity.setId(friendId);
            friendEntity.setPseudo(friendPseudo);
            mFriendDao.insert(friendEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            VbrDatabase.sDatabaseWriteExecutor.execute(runnable);
        }
    }

    public void removeFriend(final String friendId) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mFriendDao.deleteById(friendId));
    }

    public void insertFriends(final List<FriendDto> friends, boolean syncInsertion) {
        Runnable runnable = () -> {
            mFriendDao.deleteAll();
            List<FriendEntity> friendEntities = new ArrayList<>();
            for (FriendDto friend : friends) {
                FriendEntity friendEntity = new FriendEntity();
                friendEntity.setId(friend.getId());
                friendEntity.setPseudo(friend.getPseudo());
                friendEntities.add(friendEntity);
            }
            mFriendDao.insertAll(friendEntities);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            VbrDatabase.sDatabaseWriteExecutor.execute(runnable);
        }
    }

    public List<LeagueSummaryDto> listLeagues() {
        return mLeagueDao.listLeagues();
    }

    public List<LeagueSummaryDto> listLeagues(GameType kind) {
        return mLeagueDao.listLeaguesByKind(kind);
    }

    public LeagueDto getLeague(String id) {
        String json = mLeagueDao.findContentById(id);
        return JsonConverters.GSON.fromJson(json, LeagueDto.class);
    }

    public int countLeagues(String name, GameType kind) {
        return mLeagueDao.countByNameAndKind(name, kind);
    }

    public int countLeagues(String id) {
        return mLeagueDao.countById(id);
    }

    public LeagueDto getLeague(String name, GameType kind) {
        String json = mLeagueDao.findContentByNameAndKind(name, kind);
        return JsonConverters.GSON.fromJson(json, LeagueDto.class);
    }

    public void insertLeague(final LeagueDto league, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            String json = JsonConverters.GSON.toJson(league, LeagueDto.class);
            LeagueEntity leagueEntity = new LeagueEntity();
            leagueEntity.setId(league.getId());
            leagueEntity.setCreatedBy(league.getCreatedBy());
            leagueEntity.setCreatedAt(league.getCreatedAt());
            leagueEntity.setUpdatedAt(league.getUpdatedAt());
            leagueEntity.setKind(league.getKind());
            leagueEntity.setName(league.getName());
            leagueEntity.setSynced(synced);
            leagueEntity.setContent(json);
            mLeagueDao.insert(leagueEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            VbrDatabase.sDatabaseWriteExecutor.execute(runnable);
        }
    }

    public void deleteLeague(final String id) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mLeagueDao.deleteById(id));
    }

    public List<RulesSummaryDto> listRules() {
        return mRulesDao.listRules();
    }

    public List<RulesSummaryDto> listRules(GameType kind) {
        return mRulesDao.listRulesByKind(kind);
    }

    public RulesDto getRules(String id) {
        String json = mRulesDao.findContentById(id);
        return JsonConverters.GSON.fromJson(json, RulesDto.class);
    }

    public RulesDto getRules(String name, GameType kind) {
        String json = mRulesDao.findContentByNameAndKind(name, kind);
        return JsonConverters.GSON.fromJson(json, RulesDto.class);
    }

    public void insertRules(final RulesDto rules, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            String json = JsonConverters.GSON.toJson(rules);
            RulesEntity rulesEntity = new RulesEntity();
            rulesEntity.setId(rules.getId());
            rulesEntity.setCreatedBy(rules.getCreatedBy());
            rulesEntity.setCreatedAt(rules.getCreatedAt());
            rulesEntity.setUpdatedAt(rules.getUpdatedAt());
            rulesEntity.setKind(rules.getKind());
            rulesEntity.setName(rules.getName());
            rulesEntity.setSynced(synced);
            rulesEntity.setContent(json);
            mRulesDao.insert(rulesEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            VbrDatabase.sDatabaseWriteExecutor.execute(runnable);
        }
    }

    public void deleteRules(String id) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mRulesDao.deleteById(id));
    }

    public void deleteRules(Set<String> ids) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mRulesDao.deleteByIdIn(ids));
    }

    public int countRules(String id) {
        return mRulesDao.countById(id);
    }

    public int countRules(String name, GameType kind) {
        return mRulesDao.countByNameAndKind(name, kind);
    }

    public List<TeamSummaryDto> listTeams() {
        return mTeamDao.listTeams();
    }

    public List<TeamSummaryDto> listTeams(GameType kind) {
        return mTeamDao.listTeamsByKind(kind);
    }

    public List<TeamSummaryDto> listTeams(GenderType genderType, GameType kind) {
        return mTeamDao.listTeamsByGenderAndKind(genderType, kind);
    }

    public TeamDto getTeam(String id) {
        String json = mTeamDao.findContentById(id);
        return JsonConverters.GSON.fromJson(json, TeamDto.class);
    }

    public TeamDto getTeam(String name, GenderType genderType, GameType kind) {
        String json = mTeamDao.findContentByNameAndGenderAndKind(name, genderType, kind);
        return JsonConverters.GSON.fromJson(json, TeamDto.class);
    }

    public void insertTeam(final TeamDto team, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            String json = JsonConverters.GSON.toJson(team, TeamDto.class);
            TeamEntity teamEntity = new TeamEntity();
            teamEntity.setId(team.getId());
            teamEntity.setCreatedBy(team.getCreatedBy());
            teamEntity.setCreatedAt(team.getCreatedAt());
            teamEntity.setUpdatedAt(team.getUpdatedAt());
            teamEntity.setKind(team.getKind());
            teamEntity.setGender(team.getGender());
            teamEntity.setName(team.getName());
            teamEntity.setSynced(synced);
            teamEntity.setContent(json);
            mTeamDao.insert(teamEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            VbrDatabase.sDatabaseWriteExecutor.execute(runnable);
        }
    }

    public void deleteTeam(String id) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mTeamDao.deleteById(id));
    }

    public void deleteTeams(Set<String> ids) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mTeamDao.deleteByIdIn(ids));
    }

    public int countTeams(String name, GenderType genderType, GameType kind) {
        return mTeamDao.countByNameAndGenderAndKind(name, genderType, kind);
    }

    public int countTeams(String id) {
        return mTeamDao.countById(id);
    }

    public List<GameSummaryDto> listGames() {
        return mGameDao.listGames();
    }

    public IStoredGame getGame(String id) {
        String json = mGameDao.findContentById(id);
        return JsonConverters.GSON.fromJson(json, StoredGame.class);
    }

    public void insertGame(final GameDto game, boolean synced, boolean syncInsertion) {
        Runnable runnable = () -> {
            game.setScore(game.buildScore());
            GameEntity gameEntity = new GameEntity();
            gameEntity.setId(game.getId());
            gameEntity.setCreatedBy(game.getCreatedBy());
            gameEntity.setCreatedAt(game.getCreatedAt());
            gameEntity.setUpdatedAt(game.getUpdatedAt());
            gameEntity.setScheduledAt(game.getScheduledAt());
            gameEntity.setRefereedBy(game.getRefereedBy());
            gameEntity.setKind(game.getKind());
            gameEntity.setGender(game.getGender());
            gameEntity.setUsage(game.getUsage());
            gameEntity.setSynced(synced);
            if (game.getLeague() == null) {
                gameEntity.setLeagueName("");
                gameEntity.setDivisionName("");
            } else {
                gameEntity.setLeagueName(game.getLeague().getName());
                gameEntity.setDivisionName(game.getLeague().getDivision());
            }
            gameEntity.setHomeTeamName(game.getHomeTeam().getName());
            gameEntity.setGuestTeamName(game.getGuestTeam().getName());
            gameEntity.setHomeSets(game.getHomeSets());
            gameEntity.setGuestSets(game.getGuestSets());
            gameEntity.setScore(game.getScore());
            gameEntity.setContent(JsonConverters.GSON.toJson(game, GameDto.class));
            mGameDao.insert(gameEntity);
        };

        if (syncInsertion) {
            runnable.run();
        } else {
            VbrDatabase.sDatabaseWriteExecutor.execute(runnable);
        }
    }

    public void deleteGame(String id) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mGameDao.deleteById(id));
    }

    public void deleteGames(Set<String> ids) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mGameDao.deleteByIdIn(ids));
    }

    private boolean hasFullGameGame(String type) {
        return mFullGameDao.countByType(type) > 0;
    }

    private IGame getFullGame(String type) {
        String json = mFullGameDao.findContentByType(type);
        return JsonConverters.GSON.fromJson(json, new TypeToken<BaseGame>() {}.getType());
    }

    private void insertFullGame(String type, IGame game, boolean syncInsertion) {
        String json = JsonConverters.GSON.toJson(game, new TypeToken<BaseGame>() {}.getType());
        final FullGameEntity fullGameEntity = new FullGameEntity(type, json);

        if (syncInsertion) {
            mFullGameDao.insert(fullGameEntity);
        } else {
            VbrDatabase.sDatabaseWriteExecutor.execute(() -> mFullGameDao.insert(fullGameEntity));
        }
    }

    private void deleteFullGame(String type) {
        VbrDatabase.sDatabaseWriteExecutor.execute(() -> mFullGameDao.deleteByType(type));
    }

    public boolean hasCurrentGame() {
        return hasFullGameGame(sCurrentGame);
    }

    public IGame getCurrentGame() {
        return getFullGame(sCurrentGame);
    }

    public void insertCurrentGame(IGame game, boolean syncInsertion) {
        insertFullGame(sCurrentGame, game, syncInsertion);
    }

    public void deleteCurrentGame() {
        deleteFullGame(sCurrentGame);
    }

    public boolean hasSetupGame() {
        return hasFullGameGame(sSetupGame);
    }

    public IGame getSetupGame() {
        return getFullGame(sSetupGame);
    }

    public void insertSetupGame(IGame game, boolean syncInsertion) {
        insertFullGame(sSetupGame, game, syncInsertion);
    }

    public void deleteSetupGame() {
        deleteFullGame(sSetupGame);
    }
}
