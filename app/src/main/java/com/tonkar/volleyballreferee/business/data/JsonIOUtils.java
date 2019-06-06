package com.tonkar.volleyballreferee.business.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.game.BaseGame;
import com.tonkar.volleyballreferee.business.game.Set;
import com.tonkar.volleyballreferee.business.team.Player;
import com.tonkar.volleyballreferee.business.team.TeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.SubstitutionsLimitation;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.lang.reflect.Type;
import java.util.List;

public class JsonIOUtils {

    public static final Type CURRENT_GAME_TYPE            = new TypeToken<BaseGame>(){}.getType();
    public static final Type GAME_LIST_TYPE               = new TypeToken<List<StoredGame>>(){}.getType();
    public static final Type GAME_TYPE                    = new TypeToken<StoredGame>(){}.getType();
    public static final Type SET_TYPE                     = new TypeToken<ApiSet>(){}.getType();
    public static final Type TEAM_LIST_TYPE               = new TypeToken<List<ApiTeam>>(){}.getType();
    public static final Type TEAM_TYPE                    = new TypeToken<ApiTeam>(){}.getType();
    public static final Type RULES_LIST_TYPE              = new TypeToken<List<ApiRules>>(){}.getType();
    public static final Type RULES_TYPE                   = new TypeToken<ApiRules>(){}.getType();
    public static final Type GAME_DESCRIPTION_TYPE        = new TypeToken<ApiGameDescription>(){}.getType();
    public static final Type GAME_DESCRIPTION_LIST_TYPE   = new TypeToken<List<ApiGameDescription>>(){}.getType();
    public static final Type TEAM_DESCRIPTION_LIST_TYPE   = new TypeToken<List<ApiTeamDescription>>(){}.getType();
    public static final Type RULES_DESCRIPTION_LIST_TYPE  = new TypeToken<List<ApiRulesDescription>>(){}.getType();
    public static final Type LEAGUE_DESCRIPTION_LIST_TYPE = new TypeToken<List<ApiLeagueDescription>>(){}.getType();
    public static final Type LEAGUE_TYPE                  = new TypeToken<ApiLeague>(){}.getType();
    public static final Type LEAGUE_LIST_TYPE             = new TypeToken<List<ApiLeague>>(){}.getType();
    public static final Type USER_TYPE                    = new TypeToken<ApiUser>(){}.getType();
    public static final Type FRIENDS_AND_REQUESTS_TYPE    = new TypeToken<ApiFriendsAndRequests>(){}.getType();
    public static final Type MESSAGE_TYPE                 = new TypeToken<ApiMessage>(){}.getType();
    public static final Type COUNT_TYPE                   = new TypeToken<ApiCount>(){}.getType();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BaseGame.class, new InheritanceDeserializer<BaseGame>())
            .registerTypeAdapter(BaseGame.class, new InheritanceSerializer<BaseGame>())
            .registerTypeAdapter(Set.class, new InheritanceDeserializer<Set>())
            .registerTypeAdapter(Set.class, new InheritanceSerializer<Set>())
            .registerTypeAdapter(TeamComposition.class, new InheritanceDeserializer<TeamComposition>())
            .registerTypeAdapter(TeamComposition.class, new InheritanceSerializer<TeamComposition>())
            .registerTypeAdapter(TeamDefinition.class, new InheritanceDeserializer<TeamDefinition>())
            .registerTypeAdapter(TeamDefinition.class, new InheritanceSerializer<TeamDefinition>())
            .registerTypeAdapter(Player.class, new InheritanceDeserializer<Player>())
            .registerTypeAdapter(Player.class, new InheritanceSerializer<Player>())
            .registerTypeAdapter(TeamType.class, new TeamTypeDeserializer())
            .registerTypeAdapter(TeamType.class, new TeamTypeSerializer())
            .registerTypeAdapter(PositionType.class, new PositionTypeDeserializer())
            .registerTypeAdapter(PositionType.class, new PositionTypeSerializer())
            .registerTypeAdapter(SanctionType.class, new SanctionTypeDeserializer())
            .registerTypeAdapter(SanctionType.class, new SanctionTypeSerializer())
            .registerTypeAdapter(SubstitutionsLimitation.class, new InheritanceDeserializer<SubstitutionsLimitation>())
            .registerTypeAdapter(SubstitutionsLimitation.class, new InheritanceSerializer<SubstitutionsLimitation>())
            .create();

    public static class TeamTypeSerializer implements JsonSerializer<TeamType> {

        @Override
        public JsonElement serialize(TeamType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(TeamType.toLetter(src));
        }
    }

    public static class TeamTypeDeserializer implements JsonDeserializer<TeamType> {

        @Override
        public TeamType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return TeamType.fromLetter(json.getAsJsonPrimitive().getAsString());
        }
    }

    public static class PositionTypeSerializer implements JsonSerializer<PositionType> {

        @Override
        public JsonElement serialize(PositionType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(PositionType.toInt(src));
        }
    }

    public static class PositionTypeDeserializer implements JsonDeserializer<PositionType> {

        @Override
        public PositionType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return PositionType.fromInt(json.getAsJsonPrimitive().getAsInt());
        }
    }

    public static class InheritanceDeserializer<T> implements JsonDeserializer<T> {

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonPrimitive classNamePrimitive = (JsonPrimitive) jsonObject.get("classType");

            String className = classNamePrimitive.getAsString();

            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e.getMessage());
            }
            return context.deserialize(jsonObject, clazz);
        }
    }

    public static class InheritanceSerializer<T> implements JsonSerializer<T> {
        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src, src.getClass());
        }
    }

    public static class SanctionTypeSerializer implements JsonSerializer<SanctionType> {

        @Override
        public JsonElement serialize(SanctionType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(SanctionType.toLetter(src));
        }
    }

    public static class SanctionTypeDeserializer implements JsonDeserializer<SanctionType> {

        @Override
        public SanctionType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return SanctionType.fromLetter(json.getAsJsonPrimitive().getAsString());
        }
    }
}
