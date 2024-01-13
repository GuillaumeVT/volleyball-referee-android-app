package com.tonkar.volleyballreferee.engine.api;

import com.google.gson.*;
import com.tonkar.volleyballreferee.engine.game.BaseGame;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.set.Set;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.composition.TeamComposition;
import com.tonkar.volleyballreferee.engine.team.definition.TeamDefinition;
import com.tonkar.volleyballreferee.engine.team.player.*;
import com.tonkar.volleyballreferee.engine.team.substitution.SubstitutionsLimitation;

import java.lang.reflect.Type;

public class JsonConverters {

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
