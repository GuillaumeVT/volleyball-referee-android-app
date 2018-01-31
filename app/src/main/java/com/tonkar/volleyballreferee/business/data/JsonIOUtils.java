package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

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

    private static final Type RECORDED_GAME_LIST_TYPE   = new TypeToken<List<RecordedGame>>() {}.getType();
    private static final Type RECORDED_GAME_TYPE        = new TypeToken<RecordedGame>() {}.getType();
    private static final Type RECORDED_SET_TYPE         = new TypeToken<RecordedSet>() {}.getType();
    private static final Type TEAM_DEFINITION_LIST_TYPE = new TypeToken<List<IndoorTeamDefinition>>() {}.getType();

    private static final Gson sGson = new GsonBuilder()
            .registerTypeAdapter(TeamType.class, new TeamTypeSerializer())
            .registerTypeAdapter(TeamType.class, new TeamTypeDeserializer())
            .registerTypeAdapter(PositionType.class, new PositionTypeSerializer())
            .registerTypeAdapter(PositionType.class, new PositionTypeDeserializer())
            .create();

    // Read recorded games

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

    static List<RecordedGame> readRecordedGames(Context context, String fileName) {
        Log.i("VBR-Data", String.format("Read recorded games from %s", fileName));
        List<RecordedGame> games = new ArrayList<>();

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            games = readRecordedGamesStream(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("VBR-Data", String.format("%s recorded games file does not yet exist", fileName));
        } catch (IOException e) {
            Log.e("VBR-Data", "Exception while reading games", e);
        }

        return games;
    }

    public static List<RecordedGame> readRecordedGamesStream(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return sGson.fromJson(reader, RECORDED_GAME_LIST_TYPE);
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
        } catch (IOException e) {
            Log.e("VBR-Data", "Exception while writing game", e);
        }
    }

    public static void writeRecordedGamesStream(OutputStream outputStream, List<RecordedGame> recordedGames) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(recordedGames, RECORDED_GAME_LIST_TYPE, writer);
        writer.close();
    }

    static byte[] recordedGameToByteArray(RecordedGame recordedGame) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(recordedGame, RECORDED_GAME_TYPE, writer);
        writer.close();

        outputStream.close();

        return outputStream.toByteArray();
    }

    static byte[] recordedSetToByteArray(RecordedSet recordedSet) throws IOException {
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
        } catch (IOException e) {
            Log.e("VBR-SavedTeams", "Exception while reading teams", e);
        }

        return teams;
    }

    public static List<IndoorTeamDefinition> readTeamDefinitionsStream(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return sGson.fromJson(reader, TEAM_DEFINITION_LIST_TYPE);
        } finally {
            reader.close();
        }
    }

    // Write saved team

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
        } catch (IOException e) {
            Log.e("VBR-SavedTeams", "Exception while writing team", e);
        }
    }

    public static void writeTeamDefinitionsStream(OutputStream outputStream, List<IndoorTeamDefinition> teamDefinitions) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        sGson.toJson(teamDefinitions, TEAM_DEFINITION_LIST_TYPE, writer);
        writer.close();
    }
}
