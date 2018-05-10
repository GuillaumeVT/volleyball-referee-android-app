package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

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
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.game.BaseGame;
import com.tonkar.volleyballreferee.business.game.Set;
import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.business.team.Player;
import com.tonkar.volleyballreferee.business.team.TeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonIOUtils {

    private static final Type CURRENT_GAME_TYPE         = new TypeToken<BaseGame>() {}.getType();
    private static final Type RECORDED_GAME_LIST_TYPE   = new TypeToken<List<RecordedGame>>() {}.getType();
    private static final Type RECORDED_GAME_TYPE        = new TypeToken<RecordedGame>() {}.getType();
    private static final Type RECORDED_SET_TYPE         = new TypeToken<RecordedSet>() {}.getType();
    private static final Type TEAM_DEFINITION_LIST_TYPE = new TypeToken<List<IndoorTeamDefinition>>() {}.getType();
    private static final Type RULES_LIST_TYPE           = new TypeToken<List<Rules>>() {}.getType();

    private static final Gson sGson = new GsonBuilder()
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
            .create();

    // Read current game

    static GameService readCurrentGame(Context context, String fileName) {
        Log.i("VBR-Data", String.format("Read current game from %s", fileName));
        BaseGame game = null;

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            try {
                game = sGson.fromJson(reader, CURRENT_GAME_TYPE);
            } finally {
                reader.close();
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("VBR-Data", String.format("%s current game file does not yet exist", fileName));
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-Data", "Exception while reading current game", e);
        }

        return game;
    }

    // Write current game

    static void writeCurrentGame(Context context, String fileName, GameService gameService) {
        Log.i("VBR-Data", String.format("Write current game into %s", fileName));
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            sGson.toJson(gameService, CURRENT_GAME_TYPE, writer);
            writer.close();
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-Data", "Exception while writing current game", e);
        }
    }

    // Read recorded games

    static List<RecordedGame> readRecordedGames(Context context, String fileName) {
        Log.i("VBR-Data", String.format("Read recorded games from %s", fileName));
        List<RecordedGame> games = new ArrayList<>();

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            games = readRecordedGamesStream(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("VBR-Data", String.format("%s recorded games file does not yet exist", fileName));
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-Data", "Exception while reading games", e);
        }

        return games;
    }

    public static List<RecordedGame> readRecordedGamesStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return sGson.fromJson(reader, RECORDED_GAME_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    public static RecordedGame byteArrayToRecordedGame(byte[] bytes) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        try {
            return sGson.fromJson(reader, RECORDED_GAME_TYPE);
        } finally {
            reader.close();
        }
    }

    // Write recorded games

    static void writeRecordedGames(Context context, String fileName, List<RecordedGame> recordedGames) {
        Log.i("VBR-Data", String.format("Write recorded games into %s", fileName));
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writeRecordedGamesStream(outputStream, recordedGames);
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-Data", "Exception while writing games", e);
        }
    }

    public static void writeRecordedGamesStream(OutputStream outputStream, List<RecordedGame> recordedGames) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(recordedGames, RECORDED_GAME_LIST_TYPE, writer);
        writer.close();
    }

    public static byte[] recordedGameToByteArray(RecordedGame recordedGame) throws JsonParseException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(recordedGame, RECORDED_GAME_TYPE, writer);
        writer.close();

        outputStream.close();

        return outputStream.toByteArray();
    }

    static byte[] recordedSetToByteArray(RecordedSet recordedSet) throws JsonParseException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(recordedSet, RECORDED_SET_TYPE, writer);
        writer.close();

        outputStream.close();

        return outputStream.toByteArray();
    }

    // Read saved teams

    static List<SavedTeam> readSavedTeams(Context context, String fileName) {
        Log.i("VBR-SavedTeams", String.format("Read saved teams from %s", fileName));
        List<SavedTeam> teams = new ArrayList<>();

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            List<IndoorTeamDefinition> teamDefinitions = readTeamDefinitionsStream(inputStream);
            inputStream.close();

            for (IndoorTeamDefinition teamDefinition : teamDefinitions) {
                teams.add(new SavedTeam(teamDefinition));
            }
        } catch (FileNotFoundException e) {
            Log.i("VBR-SavedTeams", String.format("%s saved teams file does not yet exist", fileName));
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-SavedTeams", "Exception while reading teams", e);
        }

        return teams;
    }

    public static List<IndoorTeamDefinition> readTeamDefinitionsStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return sGson.fromJson(reader, TEAM_DEFINITION_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    // Write saved teams

    static void writeSavedTeams(Context context, String fileName, List<SavedTeam> savedTeams) {
        Log.i("VBR-SavedTeams", String.format("Write saved teams into %s", fileName));
        try {
            List<IndoorTeamDefinition> teamDefinitions = new ArrayList<>();
            for (SavedTeam savedTeam : savedTeams) {
                teamDefinitions.add(savedTeam.getIndoorTeamDefinition());
            }
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writeTeamDefinitionsStream(outputStream, teamDefinitions);
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-SavedTeams", "Exception while writing teams", e);
        }
    }

    public static void writeTeamDefinitionsStream(OutputStream outputStream, List<IndoorTeamDefinition> teamDefinitions) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(teamDefinitions, TEAM_DEFINITION_LIST_TYPE, writer);
        writer.close();
    }

    // Read saved rules

    static List<Rules> readSavedRules(Context context, String fileName) {
        Log.i("VBR-SavedRules", String.format("Read saved rules from %s", fileName));
        List<Rules> rules = new ArrayList<>();

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            rules = readRulesStream(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("VBR-SavedRules", String.format("%s saved rules file does not yet exist", fileName));
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-SavedRules", "Exception while reading rules", e);
        }

        return rules;
    }

    public static List<Rules> readRulesStream(InputStream inputStream) throws IOException, JsonParseException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return sGson.fromJson(reader, RULES_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    // Write saved rules

    static void writeSavedRules(Context context, String fileName, List<Rules> savedRules) {
        Log.i("VBR-SavedRules", String.format("Write saved rules into %s", fileName));
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writeRulesStream(outputStream, savedRules);
            outputStream.close();
        } catch (JsonParseException | IOException e) {
            Log.e("VBR-SavedRules", "Exception while writing rules", e);
        }
    }

    public static void writeRulesStream(OutputStream outputStream, List<Rules> rules) throws JsonParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(rules, RULES_LIST_TYPE, writer);
        writer.close();
    }

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
