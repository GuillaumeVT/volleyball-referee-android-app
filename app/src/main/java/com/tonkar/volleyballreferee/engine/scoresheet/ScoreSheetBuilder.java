package com.tonkar.volleyballreferee.engine.scoresheet;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import java.io.ByteArrayOutputStream;
import java.text.*;
import java.util.*;

public class ScoreSheetBuilder {

    private final Context     mContext;
    private final IStoredGame mStoredGame;
    private final String      mFilename;
    private       Document    mDocument;
    private       Element     mBody;
    private final DateFormat  mDateFormatter;
    private final DateFormat  mTimeFormatter;

    private String mLogo;
    private String mRemarks;
    private String mReferee1Signature;
    private String mReferee2Signature;
    private String mScorerSignature;
    private String mHomeCaptainSignature;
    private String mHomeCoachSignature;
    private String mGuestCaptainSignature;
    private String mGuestCoachSignature;
    private String mReferee1Name;
    private String mReferee2Name;
    private String mScorerName;
    private String mHomeCaptainName;
    private String mHomeCoachName;
    private String mGuestCaptainName;
    private String mGuestCoachName;

    public record ScoreSheet(String filename, String content) {}

    public ScoreSheet createScoreSheet() {
        mDocument = Jsoup.parse(htmlSkeleton(mFilename), "UTF-8");
        mBody = mDocument.body();

        String html = switch (mStoredGame.getKind()) {
            case INDOOR -> createStoredIndoorGame();
            case BEACH -> createStoredBeachGame();
            case INDOOR_4X4 -> createStoredIndoor4x4Game();
            case SNOW -> createStoredSnowGame();
        };

        return new ScoreSheet(mFilename, html);
    }

    public ScoreSheetBuilder(Context context, IStoredGame storedGame) {
        mContext = context;
        mStoredGame = storedGame;

        DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        String date = formatter.format(new Date(storedGame.getScheduledAt()));

        String homeTeam = storedGame.getTeamName(TeamType.HOME);
        String guestTeam = storedGame.getTeamName(TeamType.GUEST);

        String filename = String.format(Locale.getDefault(), "%s__%s__%s.html", homeTeam, guestTeam, date);
        mFilename = filename.replaceAll("[\\s|\\?\\*<:>\\+\\[\\]/\\']", "_");

        mDateFormatter = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        mDateFormatter.setTimeZone(TimeZone.getDefault());

        mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        mTimeFormatter.setTimeZone(TimeZone.getDefault());

        mRemarks = "";
    }

    public String getFilename() {
        return mFilename;
    }

    private String createStoredIndoorGame() {
        mBody.appendChild(createStoredGameHeader());
        mBody.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (setIndex % 2 == 1 || (setIndex == 0 && (mStoredGame.getPlayers(TeamType.HOME).size() > 14 || mStoredGame
                    .getPlayers(TeamType.GUEST)
                    .size() > 14)) || mStoredGame.getSubstitutions(TeamType.HOME, setIndex).size() > 6 || mStoredGame
                    .getSubstitutions(TeamType.GUEST, setIndex)
                    .size() > 6 || (mStoredGame.getPoints(TeamType.HOME, setIndex) + mStoredGame.getPoints(TeamType.GUEST,
                                                                                                           setIndex) > 64)) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            Element line2Div = new Element("div");
            line2Div.addClass("div-flex-row");
            line2Div
                    .appendChild(createStoredStartingLineup(setIndex))
                    .appendChild(createSpacingDiv())
                    .appendChild(createSpacingDiv())
                    .appendChild(createStoredSubstitutions(setIndex));
            if (mStoredGame.getRules().isTeamTimeouts()) {
                line2Div.appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredTimeouts(setIndex));
            }
            cardDiv.appendChild(line2Div);

            cardDiv.appendChild(createStoredLadder(setIndex));

            mBody.appendChild(cardDiv);
        }

        mBody.appendChild(createStoredGameHeader().addClass("new-page-for-printers"));
        mBody.appendChild(createRemarks());
        mBody.appendChild(createSignatures());
        mBody.appendChild(createFooter());

        return mDocument.toString();
    }

    private Element createStoredGameHeader() {
        Element cardDiv = new Element("div");
        cardDiv.addClass("div-card");

        if (mLogo != null) {
            Element logoDiv = new Element("div");
            logoDiv.addClass("div-grid-game-header-logo");
            logoDiv.appendChild(createLogoBox(mLogo));

            cardDiv.appendChild(logoDiv);
        }

        Element gameInfoDiv = new Element("div");
        gameInfoDiv.addClass("div-grid-game-header-info");

        SelectedLeagueDto selectedLeague = mStoredGame.getLeague();
        String league = selectedLeague == null ? "" : selectedLeague.getName() + " / " + selectedLeague.getDivision();

        gameInfoDiv.appendChild(createCellSpan(league, true, false));
        gameInfoDiv.appendChild(createCellSpan(mDateFormatter.format(new Date(mStoredGame.getStartTime())), true, false));

        String startEndTimes = String.format("%s \u2192 %s", mTimeFormatter.format(new Date(mStoredGame.getStartTime())),
                                             mTimeFormatter.format(new Date(mStoredGame.getEndTime())));
        gameInfoDiv.appendChild(createCellSpan(startEndTimes, true, false));

        int duration = (int) Math.ceil((mStoredGame.getEndTime() - mStoredGame.getStartTime()) / 60000.0);
        gameInfoDiv.appendChild(
                createCellSpan(String.format(Locale.getDefault(), mContext.getString(R.string.set_duration), duration), true, false));

        cardDiv.appendChild(gameInfoDiv);

        Element homeSetsInfoDiv = new Element("div");
        homeSetsInfoDiv.addClass("div-grid-sets-info");

        Element homeTeamNameSpan = createCellSpan(mStoredGame.getTeamName(TeamType.HOME), true, false);
        homeTeamNameSpan.addClass("vbr-home-team");
        homeSetsInfoDiv.appendChild(homeTeamNameSpan);

        homeSetsInfoDiv.appendChild(createCellSpan(String.valueOf(mStoredGame.getSets(TeamType.HOME)), true, false));

        int homePointsTotal = 0;

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            int points = mStoredGame.getPoints(TeamType.HOME, setIndex);
            homePointsTotal += points;
            homeSetsInfoDiv.appendChild(createSetCellAnchor(String.valueOf(points), setIndex));
        }

        homeSetsInfoDiv.appendChild(createCellSpan(String.valueOf(homePointsTotal), true, false));

        cardDiv.appendChild(homeSetsInfoDiv);

        Element guestSetsInfoDiv = new Element("div");
        guestSetsInfoDiv.addClass("div-grid-sets-info");

        Element guestTeamNameSpan = createCellSpan(mStoredGame.getTeamName(TeamType.GUEST), true, false);
        guestTeamNameSpan.addClass("vbr-guest-team");
        guestSetsInfoDiv.appendChild(guestTeamNameSpan);

        guestSetsInfoDiv.appendChild(createCellSpan(String.valueOf(mStoredGame.getSets(TeamType.GUEST)), true, false));

        int guestPointsTotal = 0;

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            int points = mStoredGame.getPoints(TeamType.GUEST, setIndex);
            guestPointsTotal += points;
            guestSetsInfoDiv.appendChild(createSetCellAnchor(String.valueOf(points), setIndex));
        }

        guestSetsInfoDiv.appendChild(createCellSpan(String.valueOf(guestPointsTotal), true, false));

        cardDiv.appendChild(guestSetsInfoDiv);

        return cardDiv;
    }

    private Element createStoredTeams() {
        Element cardDiv = new Element("div");
        cardDiv.addClass("div-card").addClass("spacing-before");

        cardDiv.appendChild(createTitleDiv(mContext.getString(R.string.players)));

        Element teamsDiv = new Element("div");
        teamsDiv.addClass("div-grid-h-g");
        teamsDiv.appendChild(createTeamDiv(TeamType.HOME)).appendChild(createSpacingDiv()).appendChild(createTeamDiv(TeamType.GUEST));
        cardDiv.appendChild(teamsDiv);

        return cardDiv;
    }

    private Element createTeamDiv(TeamType teamType) {
        Element teamDiv = new Element("div");
        teamDiv.addClass("div-grid-team");

        for (PlayerDto player : mStoredGame.getPlayers(teamType)) {
            teamDiv.appendChild(createPlayerSpan(teamType, player.getNum(), mStoredGame.isLibero(teamType, player.getNum())));
            teamDiv.appendChild(createCellSpan(player.getName(), false, false));
        }

        return teamDiv;
    }

    private Element createTitleDiv(String title) {
        Element titleDiv = new Element("div");
        titleDiv.addClass("div-title");
        titleDiv.appendChild(createCellSpan(title, false, false));
        return titleDiv;
    }

    private Element createSetCellAnchor(String text, int setIndex) {
        Element anchor = new Element("a");
        anchor.addClass("bordered-cell").addClass("set-anchor");
        anchor.attr("href", String.format(Locale.getDefault(), "#div-set-%d", (1 + setIndex)));
        anchor.appendText(text);
        return anchor;
    }

    private Element createCellSpan(String text, boolean withBorder, boolean isBadge) {
        Element span = new Element("span");
        span.addClass(isBadge ? "badge" : (withBorder ? "bordered-cell" : "cell"));
        span.appendText(text);
        return span;
    }

    private Element createPlayerSpan(TeamType teamType, int player, boolean isLibero) {
        String playerStr = String.valueOf(player);

        if (player < 0 || player == SanctionDto.TEAM) {
            playerStr = "-";
        } else if (player == SanctionDto.COACH) {
            playerStr = mContext.getString(R.string.coach_abbreviation);
        }

        Element playerSpan = createCellSpan(playerStr, false, true);

        if (isLibero) {
            playerSpan.addClass(TeamType.HOME.equals(teamType) ? "vbr-home-libero" : "vbr-guest-libero");
        } else {
            playerSpan.addClass(TeamType.HOME.equals(teamType) ? "vbr-home-team" : "vbr-guest-team");
        }

        if (mStoredGame.getCaptain(teamType) == player) {
            playerSpan.addClass("vbr-captain");
        }

        return playerSpan;
    }

    private Element createEmptyPlayerSpan(TeamType teamType) {
        Element playerSpan = createCellSpan("-", false, true);
        playerSpan.addClass(TeamType.HOME.equals(teamType) ? "vbr-home-team" : "vbr-guest-team");
        return playerSpan;
    }

    private Element createStoredSetHeader(int setIndex) {
        Element setHeaderDiv = new Element("div");
        setHeaderDiv.addClass("div-flex-row");

        Element setInfoDiv = new Element("div");
        setInfoDiv.addClass("div-grid-set-header-info");

        Element indexSpan = createCellSpan(String.format(Locale.getDefault(), mContext.getString(R.string.set_number), (setIndex + 1)),
                                           true, false);
        indexSpan
                .addClass("set-index-cell")
                .addClass((mStoredGame.getPoints(TeamType.HOME, setIndex) > mStoredGame.getPoints(TeamType.GUEST,
                                                                                                  setIndex)) ? "vbr-home-team" : "vbr-guest-team");
        setInfoDiv.appendChild(indexSpan);

        setInfoDiv.appendChild(createCellSpan(String.valueOf(mStoredGame.getPoints(TeamType.HOME, setIndex)), true, false));
        setInfoDiv.appendChild(createCellSpan(String.valueOf(mStoredGame.getPoints(TeamType.GUEST, setIndex)), true, false));

        setHeaderDiv.appendChild(setInfoDiv);

        setHeaderDiv.appendChild(createSpacingDiv());
        setHeaderDiv.appendChild(createSpacingDiv());

        Element setTimeDiv = new Element("div");
        setTimeDiv.addClass("div-grid-set-header-time");

        String startEndTimes = String.format("%s \u2192 %s", mTimeFormatter.format(new Date(mStoredGame.getSetStartTime(setIndex))),
                                             mTimeFormatter.format(new Date(mStoredGame.getSetEndTime(setIndex))));
        int duration = (int) Math.ceil(mStoredGame.getSetDuration(setIndex) / 60000.0);

        setTimeDiv.appendChild(createCellSpan(startEndTimes, true, false));
        setTimeDiv.appendChild(
                createCellSpan(String.format(Locale.getDefault(), mContext.getString(R.string.set_duration), duration), true, false));

        setHeaderDiv.appendChild(setTimeDiv);

        setHeaderDiv.appendChild(createSpacingDiv());
        setHeaderDiv.appendChild(createSpacingDiv());

        if (mStoredGame.getRules().isSanctions()) {
            setHeaderDiv.appendChild(createStoredSanctions(setIndex));
        }

        return setHeaderDiv;
    }

    private Element createStoredStartingLineup(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv(mContext.getString(R.string.confirm_lineup_title)).addClass("spacing-before"));

        Element lineupsDiv = new Element("div");
        lineupsDiv.addClass("div-grid-h-g");
        lineupsDiv
                .appendChild(createLineupDiv(TeamType.HOME, setIndex))
                .appendChild(createEmptyDiv())
                .appendChild(createLineupDiv(TeamType.GUEST, setIndex));
        wrapperDiv.appendChild(lineupsDiv);

        return wrapperDiv;
    }

    private Element createLineupDiv(TeamType teamType, int setIndex) {
        Element lineupDiv = new Element("div");
        lineupDiv.addClass("div-grid-lineup").addClass("border");

        if (mStoredGame.isStartingLineupConfirmed(teamType, setIndex)) {
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_4_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_3_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_2_title), false, false));

            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_4, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_3, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_2, setIndex),
                                     false));

            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_5_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_6_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_1_title), false, false));

            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_5, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_6, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_1, setIndex),
                                     false));
        } else {
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_4_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_3_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_2_title), false, false));

            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));

            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_5_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_6_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_1_title), false, false));

            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
        }

        return lineupDiv;
    }

    private Element createStoredSubstitutions(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv(mContext.getString(R.string.substitutions_tab)).addClass("spacing-before"));

        Element substitutionsDiv = new Element("div");
        substitutionsDiv.addClass("div-grid-h-g");

        substitutionsDiv
                .appendChild(createSubstitutionsDiv(TeamType.HOME, setIndex))
                .appendChild(createEmptyDiv())
                .appendChild(createSubstitutionsDiv(TeamType.GUEST, setIndex));

        wrapperDiv.appendChild(substitutionsDiv);

        return wrapperDiv;
    }

    private Element createSubstitutionsDiv(TeamType teamType, int setIndex) {
        Element substitutionsDiv = new Element("div");
        substitutionsDiv.addClass("div-flex-column");

        for (SubstitutionDto substitution : mStoredGame.getSubstitutions(teamType, setIndex)) {
            substitutionsDiv.appendChild(createSubstitutionDiv(teamType, substitution));
        }

        return substitutionsDiv;
    }

    private Element createSubstitutionDiv(TeamType teamType, SubstitutionDto substitution) {
        Element substitutionDiv = new Element("div");
        substitutionDiv.addClass("div-grid-substitution");

        String score = String.format(Locale.getDefault(), "%d-%d",
                                     TeamType.HOME.equals(teamType) ? substitution.getHomePoints() : substitution.getGuestPoints(),
                                     TeamType.HOME.equals(teamType) ? substitution.getGuestPoints() : substitution.getHomePoints());

        substitutionDiv.appendChild(createPlayerSpan(teamType, substitution.getPlayerIn(), false));
        substitutionDiv.appendChild(new Element("div").addClass("substitution-image"));
        substitutionDiv.appendChild(createPlayerSpan(teamType, substitution.getPlayerOut(), false));
        substitutionDiv.appendChild(createCellSpan(score, false, false));

        return substitutionDiv;
    }

    private Element createStoredTimeouts(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv(mContext.getString(R.string.timeouts_tab)).addClass("spacing-before"));

        Element timeoutsDiv = new Element("div");
        timeoutsDiv.addClass("div-grid-h-g");
        timeoutsDiv
                .appendChild(createTimeoutsDiv(TeamType.HOME, setIndex))
                .appendChild(createEmptyDiv())
                .appendChild(createTimeoutsDiv(TeamType.GUEST, setIndex));

        wrapperDiv.appendChild(timeoutsDiv);

        return wrapperDiv;
    }

    private Element createTimeoutsDiv(TeamType teamType, int setIndex) {
        Element timeoutsDiv = new Element("div");
        timeoutsDiv.addClass("div-flex-column");

        for (TimeoutDto timeout : mStoredGame.getCalledTimeouts(teamType, setIndex)) {
            timeoutsDiv.appendChild(createTimeoutDiv(teamType, timeout));
        }

        return timeoutsDiv;
    }

    private Element createTimeoutDiv(TeamType teamType, TimeoutDto timeout) {
        Element timeoutDiv = new Element("div");
        timeoutDiv.addClass("div-grid-timeout");

        String score = String.format(Locale.getDefault(), "%d-%d",
                                     TeamType.HOME.equals(teamType) ? timeout.getHomePoints() : timeout.getGuestPoints(),
                                     TeamType.HOME.equals(teamType) ? timeout.getGuestPoints() : timeout.getHomePoints());

        timeoutDiv.appendChild(createPlayerSpan(teamType, -1, false).addClass(getTimeoutImageClass(mStoredGame.getTeamColor(teamType))));
        timeoutDiv.appendChild(createCellSpan(score, false, false));

        return timeoutDiv;
    }

    private String getTimeoutImageClass(int backgroundColor) {
        String imageClass;

        double a = 1 - (0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(
                backgroundColor)) / 255;

        if (a < 0.5) {
            imageClass = "timeout-gray-image";
        } else {
            imageClass = "timeout-white-image";
        }

        return imageClass;
    }

    private Element createStoredSanctions(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv(mContext.getString(R.string.sanctions_tab)));

        Element sanctionsDiv = new Element("div");
        sanctionsDiv.addClass("div-grid-h-g");
        sanctionsDiv
                .appendChild(createSanctionsDiv(TeamType.HOME, setIndex))
                .appendChild(createEmptyDiv())
                .appendChild(createSanctionsDiv(TeamType.GUEST, setIndex));

        wrapperDiv.appendChild(sanctionsDiv);

        return wrapperDiv;
    }

    private Element createSanctionsDiv(TeamType teamType, int setIndex) {
        Element sanctionsDiv = new Element("div");
        sanctionsDiv.addClass("div-flex-column");

        for (SanctionDto sanction : mStoredGame.getAllSanctions(teamType, setIndex)) {
            sanctionsDiv.appendChild(createSanctionDiv(teamType, sanction));
        }

        return sanctionsDiv;
    }

    private Element createSanctionDiv(TeamType teamType, SanctionDto sanction) {
        Element sanctionDiv = new Element("div");
        sanctionDiv.addClass("div-grid-sanction");

        int player = sanction.getNum();

        String score = String.format(Locale.getDefault(), "%d-%d",
                                     TeamType.HOME.equals(teamType) ? sanction.getHomePoints() : sanction.getGuestPoints(),
                                     TeamType.HOME.equals(teamType) ? sanction.getGuestPoints() : sanction.getHomePoints());

        sanctionDiv.appendChild(new Element("div").addClass(getSanctionImageClass(sanction.getCard())));
        sanctionDiv.appendChild(createPlayerSpan(teamType, player, mStoredGame.isLibero(teamType, player)));
        sanctionDiv.appendChild(createCellSpan(score, false, false));

        return sanctionDiv;
    }

    private String getSanctionImageClass(SanctionType sanctionType) {
        return switch (sanctionType) {
            case YELLOW -> "yellow-card-image";
            case RED -> "red-card-image";
            case RED_EXPULSION -> "expulsion-card-image";
            case RED_DISQUALIFICATION -> "disqualification-card-image";
            case DELAY_WARNING -> "delay-warning-image";
            default -> "delay-penalty-image";
        };
    }

    private Element createStoredLadder(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv(mContext.getString(R.string.ladder_tab)).addClass("spacing-before"));

        int homeScore = 0;
        int guestScore = 0;

        Element ladderDiv = new Element("div");
        ladderDiv.addClass("div-flex-row");

        TeamType firstServingTeam = mStoredGame.getFirstServingTeam(setIndex);
        ladderDiv.appendChild(createServiceLadderItem(firstServingTeam));

        for (TeamType teamType : mStoredGame.getPointsLadder(setIndex)) {
            if (TeamType.HOME.equals(teamType)) {
                homeScore++;
                ladderDiv.appendChild(createLadderItem(teamType, homeScore));
            } else {
                guestScore++;
                ladderDiv.appendChild(createLadderItem(teamType, guestScore));
            }
        }

        wrapperDiv.appendChild(ladderDiv);

        return wrapperDiv;
    }

    private Element createLadderItem(TeamType teamType, int score) {
        Element ladderItemDiv = new Element("div");
        ladderItemDiv.addClass("div-flex-column").addClass("ladder-spacing");

        if (TeamType.HOME.equals(teamType)) {
            ladderItemDiv.appendChild(createCellSpan(String.valueOf(score), false, true).addClass("vbr-home-team"));
            ladderItemDiv.appendChild(createCellSpan(" ", false, true));
        } else {
            ladderItemDiv.appendChild(createCellSpan(" ", false, true));
            ladderItemDiv.appendChild(createCellSpan(String.valueOf(score), false, true).addClass("vbr-guest-team"));
        }

        return ladderItemDiv;
    }

    private Element createServiceLadderItem(TeamType teamType) {
        Element ladderItemDiv = new Element("div");
        ladderItemDiv.addClass("div-flex-column").addClass("ladder-spacing");

        if (TeamType.HOME.equals(teamType)) {
            ladderItemDiv.appendChild(createCellSpan(" ", false, true)
                                              .addClass("vbr-home-team")
                                              .appendChild(new Element("span").addClass(
                                                      getServiceImageClass(mStoredGame.getTeamColor(teamType)))));
            ladderItemDiv.appendChild(createCellSpan(" ", false, true));
        } else {
            ladderItemDiv.appendChild(createCellSpan(" ", false, true));
            ladderItemDiv.appendChild(createCellSpan(" ", false, true)
                                              .addClass("vbr-guest-team")
                                              .appendChild(new Element("span").addClass(
                                                      getServiceImageClass(mStoredGame.getTeamColor(teamType)))));
        }

        return ladderItemDiv;
    }

    private String getServiceImageClass(int backgroundColor) {
        String imageClass;

        double a = 1 - (0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(
                backgroundColor)) / 255;

        if (a < 0.5) {
            imageClass = "service-gray-image";
        } else {
            imageClass = "service-white-image";
        }

        return imageClass;
    }

    private Element createRemarks() {
        Element cardDiv = new Element("div");
        cardDiv.addClass("div-card").addClass("spacing-before");

        cardDiv.appendChild(createTitleDiv(mContext.getString(R.string.remarks)));

        Element remarksDiv = new Element("div");
        remarksDiv.addClass("remarks-cell").addClass("spacing-before");

        for (String line : mRemarks.split("\n")) {
            remarksDiv.appendText(line).appendElement("br");
        }

        cardDiv.appendChild(remarksDiv);

        return cardDiv;
    }

    private Element createSignatures() {
        Element cardDiv = new Element("div");
        cardDiv.addClass("div-card").addClass("spacing-before");

        cardDiv.appendChild(createTitleDiv(mContext.getString(R.string.signatures)));

        Element refereeSignaturesDiv = new Element("div");
        refereeSignaturesDiv.addClass("div-grid-1-2-3").addClass("spacing-before");
        refereeSignaturesDiv
                .appendChild(createRefereeSignatureDiv(1))
                .appendChild(createSpacingDiv())
                .appendChild(createRefereeSignatureDiv(2))
                .appendChild(createEmptyDiv())
                .appendChild(createScorerSignatureDiv());
        cardDiv.appendChild(refereeSignaturesDiv);

        Element captainSignaturesDiv = new Element("div");
        captainSignaturesDiv.addClass("div-grid-1-2-3").addClass("spacing-before");
        captainSignaturesDiv
                .appendChild(createCaptainSignatureDiv(TeamType.HOME))
                .appendChild(createSpacingDiv())
                .appendChild(createCaptainSignatureDiv(TeamType.GUEST));
        cardDiv.appendChild(captainSignaturesDiv);

        if (GameType.INDOOR.equals(mStoredGame.getKind()) || GameType.INDOOR_4X4.equals(mStoredGame.getKind())) {
            Element coachSignaturesDiv = new Element("div");
            coachSignaturesDiv.addClass("div-grid-1-2-3").addClass("spacing-before");
            coachSignaturesDiv
                    .appendChild(createCoachSignaturesDiv(TeamType.HOME))
                    .appendChild(createSpacingDiv())
                    .appendChild(createCoachSignaturesDiv(TeamType.GUEST));
            cardDiv.appendChild(coachSignaturesDiv);
        }

        return cardDiv;
    }

    private Element createRefereeSignatureDiv(int number) {
        Element signatureDiv = new Element("div");
        signatureDiv.addClass("div-grid-signature");

        signatureDiv.appendChild(
                createSignatureTitleBox(String.format(Locale.getDefault(), "%s %d", mContext.getString(R.string.referee), number)));
        signatureDiv.appendChild(createSignatureNameBox(number == 1 ? mReferee1Name : mReferee2Name));
        signatureDiv.appendChild(createSignatureBoxWithImage(number == 1 ? mReferee1Signature : mReferee2Signature));

        return signatureDiv;
    }

    private Element createScorerSignatureDiv() {
        Element signatureDiv = new Element("div");
        signatureDiv.addClass("div-grid-signature");

        signatureDiv.appendChild(createSignatureTitleBox(mContext.getString(R.string.scorer)));
        signatureDiv.appendChild(createSignatureNameBox(mScorerName));
        signatureDiv.appendChild(createSignatureBoxWithImage(mScorerSignature));

        return signatureDiv;
    }

    private Element createCaptainSignatureDiv(TeamType teamType) {
        Element signatureDiv = new Element("div");
        signatureDiv.addClass("div-grid-signature");

        if (TeamType.HOME.equals(teamType)) {
            signatureDiv.appendChild(createSignatureTitleBox(mContext.getString(R.string.captain)).addClass("vbr-home-team"));
            signatureDiv.appendChild(createSignatureNameBox(mHomeCaptainName));
            signatureDiv.appendChild(createSignatureBoxWithImage(mHomeCaptainSignature));
        } else {
            signatureDiv.appendChild(createSignatureTitleBox(mContext.getString(R.string.captain)).addClass("vbr-guest-team"));
            signatureDiv.appendChild(createSignatureNameBox(mGuestCaptainName));
            signatureDiv.appendChild(createSignatureBoxWithImage(mGuestCaptainSignature));
        }

        return signatureDiv;
    }

    private Element createCoachSignaturesDiv(TeamType teamType) {
        Element signatureDiv = new Element("div");
        signatureDiv.addClass("div-grid-signature");

        if (TeamType.HOME.equals(teamType)) {
            signatureDiv.appendChild(createSignatureTitleBox(mContext.getString(R.string.coach)).addClass("vbr-home-team"));
            signatureDiv.appendChild(createSignatureNameBox(mHomeCoachName));
            signatureDiv.appendChild(createSignatureBoxWithImage(mHomeCoachSignature));
        } else {
            signatureDiv.appendChild(createSignatureTitleBox(mContext.getString(R.string.coach)).addClass("vbr-guest-team"));
            signatureDiv.appendChild(createSignatureNameBox(mGuestCoachName));
            signatureDiv.appendChild(createSignatureBoxWithImage(mGuestCoachSignature));
        }

        return signatureDiv;
    }

    private Element createSignatureTitleBox(String text) {
        Element div = new Element("div");
        div.addClass("signature-title-cell");
        div.appendText(text);
        return div;
    }

    private Element createSignatureNameBox(String text) {
        if (text == null || text.isEmpty()) {
            text = "";
        }

        Element div = new Element("div");
        div.addClass("signature-name-cell");
        div.appendText(text);
        return div;
    }

    private Element createEmptySignatureBox() {
        Element div = new Element("div");
        div.addClass("signature-cell");
        div.appendText(" ");
        return div;
    }

    private Element createSignatureBoxWithImage(String base64Image) {
        Element div;

        if (base64Image == null) {
            div = createEmptySignatureBox();
        } else {
            Element img = new Element("img");
            img.addClass("signature-image");
            img.attr("src", String.format("data:image/png;base64,%s", base64Image));

            div = new Element("div");
            div.addClass("signature-cell");
            div.appendChild(img);
        }

        return div;
    }

    private Element createLogoBox(String base64Image) {
        Element img = new Element("img");
        img.addClass("logo-image");
        img.attr("src", String.format("data:image/jpeg;base64,%s", base64Image));
        return img;
    }

    private Element createFooter() {
        Element div = new Element("div");
        div.addClass("div-footer");
        div.appendText("Powered by Volleyball Referee");

        Element vbrLogo = new Element("div");
        vbrLogo.addClass("vbr-logo-image");
        div.appendChild(vbrLogo);

        return div;
    }

    private String createStoredIndoor4x4Game() {
        mBody.appendChild(createStoredGameHeader());
        mBody.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (setIndex % 2 == 1 || (setIndex == 0 && (mStoredGame.getPlayers(TeamType.HOME).size() > 14 || mStoredGame
                    .getPlayers(TeamType.GUEST)
                    .size() > 14)) || mStoredGame.getSubstitutions(TeamType.HOME, setIndex).size() > 6 || mStoredGame
                    .getSubstitutions(TeamType.GUEST, setIndex)
                    .size() > 6 || (mStoredGame.getPoints(TeamType.HOME, setIndex) + mStoredGame.getPoints(TeamType.GUEST,
                                                                                                           setIndex) > 64)) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            Element line2Div = new Element("div");
            line2Div.addClass("div-flex-row");
            line2Div
                    .appendChild(createStoredStartingLineup4x4(setIndex))
                    .appendChild(createSpacingDiv())
                    .appendChild(createSpacingDiv())
                    .appendChild(createStoredSubstitutions(setIndex));
            if (mStoredGame.getRules().isTeamTimeouts()) {
                line2Div.appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredTimeouts(setIndex));
            }
            cardDiv.appendChild(line2Div);

            cardDiv.appendChild(createStoredLadder(setIndex));

            mBody.appendChild(cardDiv);
        }

        mBody.appendChild(createStoredGameHeader().addClass("new-page-for-printers"));
        mBody.appendChild(createRemarks());
        mBody.appendChild(createSignatures());
        mBody.appendChild(createFooter());

        return mDocument.toString();
    }

    private Element createStoredStartingLineup4x4(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv(mContext.getString(R.string.confirm_lineup_title)).addClass("spacing-before"));

        Element lineupsDiv = new Element("div");
        lineupsDiv.addClass("div-grid-h-g");
        lineupsDiv
                .appendChild(createLineupDiv4x4(TeamType.HOME, setIndex))
                .appendChild(createEmptyDiv())
                .appendChild(createLineupDiv4x4(TeamType.GUEST, setIndex));
        wrapperDiv.appendChild(lineupsDiv);

        return wrapperDiv;
    }

    private Element createLineupDiv4x4(TeamType teamType, int setIndex) {
        Element lineupDiv = new Element("div");
        lineupDiv.addClass("div-grid-lineup").addClass("border");

        if (mStoredGame.isStartingLineupConfirmed(teamType, setIndex)) {
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_4_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_3_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_2_title), false, false));

            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_4, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_3, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_2, setIndex),
                                     false));

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_1_title), false, false));
            lineupDiv.appendChild(createEmptyDiv());

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_1, setIndex),
                                     false));
            lineupDiv.appendChild(createEmptyDiv());
        } else {
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_4_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_3_title), false, false));
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_2_title), false, false));

            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_1_title), false, false));
            lineupDiv.appendChild(createEmptyDiv());

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyDiv());
        }

        return lineupDiv;
    }

    private String createStoredBeachGame() {
        mBody.appendChild(createStoredGameHeader());
        mBody.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (mStoredGame.getNumberOfSets() > 2 && setIndex % 2 == 1) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            if (mStoredGame.getRules().isTeamTimeouts()) {
                Element timeoutDiv = new Element("div");
                timeoutDiv.addClass("div-flex-row");
                timeoutDiv.appendChild(createStoredTimeouts(setIndex));
                cardDiv.appendChild(timeoutDiv);
            }

            cardDiv.appendChild(createStoredLadder(setIndex));

            mBody.appendChild(cardDiv);
        }

        mBody.appendChild(createStoredGameHeader().addClass("new-page-for-printers"));
        mBody.appendChild(createRemarks());
        mBody.appendChild(createSignatures());
        mBody.appendChild(createFooter());

        return mDocument.toString();
    }

    private String createStoredSnowGame() {
        mBody.appendChild(createStoredGameHeader());
        mBody.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (mStoredGame.getNumberOfSets() > 2 && setIndex % 2 == 1) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            Element line2Div = new Element("div");
            line2Div.addClass("div-flex-row");
            line2Div
                    .appendChild(createStoredStartingLineupSnow(setIndex))
                    .appendChild(createSpacingDiv())
                    .appendChild(createSpacingDiv())
                    .appendChild(createStoredSubstitutions(setIndex));
            if (mStoredGame.getRules().isTeamTimeouts()) {
                line2Div.appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredTimeouts(setIndex));
            }
            cardDiv.appendChild(line2Div);

            cardDiv.appendChild(createStoredLadder(setIndex));

            mBody.appendChild(cardDiv);
        }

        mBody.appendChild(createStoredGameHeader().addClass("new-page-for-printers"));
        mBody.appendChild(createRemarks());
        mBody.appendChild(createSignatures());
        mBody.appendChild(createFooter());

        return mDocument.toString();
    }

    private Element createStoredStartingLineupSnow(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv(mContext.getString(R.string.confirm_lineup_title)).addClass("spacing-before"));

        Element lineupsDiv = new Element("div");
        lineupsDiv.addClass("div-grid-h-g");
        lineupsDiv
                .appendChild(createLineupDivSnow(TeamType.HOME, setIndex))
                .appendChild(createEmptyDiv())
                .appendChild(createLineupDivSnow(TeamType.GUEST, setIndex));
        wrapperDiv.appendChild(lineupsDiv);

        return wrapperDiv;
    }

    private Element createLineupDivSnow(TeamType teamType, int setIndex) {
        Element lineupDiv = new Element("div");
        lineupDiv.addClass("div-grid-lineup").addClass("border");

        lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_1_title), false, false));
        lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_2_title), false, false));
        lineupDiv.appendChild(createCellSpan(mContext.getString(R.string.position_3_title), false, false));

        if (mStoredGame.isStartingLineupConfirmed(teamType, setIndex)) {
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_1, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_2, setIndex),
                                     false));
            lineupDiv.appendChild(
                    createPlayerSpan(teamType, mStoredGame.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_3, setIndex),
                                     false));
        } else {
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
        }

        return lineupDiv;
    }

    private Element createSpacingDiv() {
        Element div = new Element("div");
        div.addClass("horizontal-spacing");
        return div;
    }

    private Element createEmptyDiv() {
        return new Element("div");
    }

    public void setLogo(String base64Image) {
        mLogo = base64Image;
    }

    public void setReferee1Signature(String name, String base64Image) {
        mReferee1Name = name;
        mReferee1Signature = base64Image;
    }

    public void setReferee2Signature(String name, String base64Image) {
        mReferee2Name = name;
        mReferee2Signature = base64Image;
    }

    public void setScorerSignature(String name, String base64Image) {
        mScorerName = name;
        mScorerSignature = base64Image;
    }

    public void setHomeCaptainSignature(String name, String base64Image) {
        mHomeCaptainName = name;
        mHomeCaptainSignature = base64Image;
    }

    public void setHomeCoachSignature(String name, String base64Image) {
        mHomeCoachName = name;
        mHomeCoachSignature = base64Image;
    }

    public void setGuestCaptainSignature(String name, String base64Image) {
        mGuestCaptainName = name;
        mGuestCaptainSignature = base64Image;
    }

    public void setGuestCoachSignature(String name, String base64Image) {
        mGuestCoachName = name;
        mGuestCoachSignature = base64Image;
    }

    public void setRemarks(String text) {
        mRemarks = text;
    }

    public String getRemarks() {
        return mRemarks;
    }

    private String toBase64(@DrawableRes int resource, int widthPixels, int heightPixels) {
        Drawable drawable = AppCompatResources.getDrawable(mContext, resource);
        Bitmap bitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }

    private String colorIntToHtml(int color) {
        return String.format("#%06X", (0xFFFFFF & color)).toLowerCase();
    }

    private String htmlSkeleton(String title) {
        int homeColor = mStoredGame.getTeamColor(TeamType.HOME);
        int guestColor = mStoredGame.getTeamColor(TeamType.GUEST);

        if (homeColor == guestColor) {
            guestColor = ContextCompat.getColor(mContext, R.color.colorReportDuplicate);
        }

        String homeTeamBackgroundColor = colorIntToHtml(homeColor);
        String homeTeamColor = colorIntToHtml(UiUtils.getTextColor(mContext, homeColor));
        String homeLiberoBackgroundColor = colorIntToHtml(mStoredGame.getLiberoColor(TeamType.HOME));
        String homeLiberoColor = colorIntToHtml(UiUtils.getTextColor(mContext, mStoredGame.getLiberoColor(TeamType.HOME)));

        String guestTeamBackgroundColor = colorIntToHtml(guestColor);
        String guestTeamColor = colorIntToHtml(UiUtils.getTextColor(mContext, guestColor));
        String guestLiberoBackgroundColor = colorIntToHtml(mStoredGame.getLiberoColor(TeamType.GUEST));
        String guestLiberoColor = colorIntToHtml(UiUtils.getTextColor(mContext, mStoredGame.getLiberoColor(TeamType.GUEST)));

        return "<!doctype html>\n" + "<html>\n" + "  <head>\n" + "    <meta charset=\"utf-8\">" + "    <title>" + title + "</title>" + "    <link href=\"https://fonts.googleapis.com/css?family=Roboto:400,700\" rel=\"stylesheet\">" + "    <meta name=\"theme-color\" content=\"#1f4294\">" + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" + "    <link rel=\"icon\" type=\"image/x-icon\" href=\"data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAJAAAACQCAYAAADnRuK4AAAtjUlEQVR42u1dB3hUVdqWLiKgCMISEAiRXlWwgGtHsK2K7ArYsDdUwH9VXHcprqKLqHSQLhBBOlKEkEASEkhCCmmEVFJJSJ2SqeH85z05N965905LpiVmnuc8iTiZufec937f+/Vrrml+Nb+aX82v5lfzq/nVWF+EkBZ0teSrlZ0lvK9F8879OUECELTmq1VDgMA/U/p5zeBqYoBhB+vAezvS1Z2uPnQNomskXbfxNZL/Wx/+no72QCICVjOgGhloWlkDDP33bnSNpesFuubRtZGuILri6cqhq5wuHV0mumrIH68a/m86/p4c/jdB/DPm8c/EZ3ezAahWzWBqJKDhTz6kxkt0raIrkq5iovC6evVqvZfCC98RQddK/t24hpbNYPI94DD1JPk3qJZH6VpCVwyXGEpAqaEvE19mvoTXVTtLeAl/J3xOjRVg6fi1LOHX1lFyza2lAGt+eVDa8AN4kK7ldGUoAMYsAooAEOKmdVUELvadClIqg1/rA+IHoFkquR88FpvLCe1cuhIloMFLAIw7weIMqBig+LWJLzeR30Mf6UPSfOLuA844urbQpZFIGhN/+p064LLKapKeU0aOhWWSVYExZMmmMzbXsq1R7L0hZ7JJZm45uVyqJiaT2ZnvrOFgEqs6Db+ncc1Aci1wxKrqYbqOSaSNIGlsH5pJR0xVifSnVvb/Zsw9SEY9/RMZPGk16ffQctLngWU2V98Hl7P3DnlsNRlN/+6uv28kT7/3K/lkcTBZtyuWhJzNIRmXyohKo3cETIJkEt8W7vERa/vQ/HKM54i5wd10HVIAjnX1ZDYSs1FNjFVJRH9pE9Gef4+oTt1BTKoLFu8zGE1k/NTNdkHjzBo4cRV54OWt5FUKzB82nyURsXmkSq1j32VHzUmBhHu+R8L1mvmRHfCIgdOXi3UxvTHZeqLNuhJiKDxAtMmfENXpB0nlMT+6erJVFTyEmNTpFu/Pyqsgg6g0cSWAlNZfX9hCPv0umARFZJGSMo09qWSSECXsQV+lPWp+KUgd/E438Z/0Z5UIOVbUlJmY9WXEWBlHtKlfEHXEw6TqRIAFcISlCr+fmKsLLP7+BD3QWyesdDuAalXfMjLsiTXk4RnbyIKVoSThwmWitq3mxBZcFd+TFs3SSIHriH6/h/tM7Koqs6ma6PN/JZqYaUy6SAEjXXif2VBh8Rl7g1JJwCMrPAIg6Rr51Foy6+tjJCzmki31JlVt5yRqrdWfHTxtRL8vEIltKxaVmZg0WaT64rdEFTbeLmgsABT/BqmhvKgOgOYasmhtOJMM3gCQeE3+YBfZf+ICqVTprFpueJhE+7NAaQ//dA5B/ns/uk5JpI6MFAM4usxlVBXdRyqP93YKPFBp2uR/MmtMDKB5y055HTzCglU3bc5eplbVWr0tfiRsFfasn5Kro8mHIITfzWbz3+l/V4qkjkxdmQ2VRJe9xmmJY7GO9yK6jO8tP9fHACQs8KSPvjpG0rJL2TUqqTWRNKo0GAz/UNrbPwPfWWRL6sAM1+duISpKjJ2XOPYBpDeYyMffBPkcgATCPfqZdcxxWXRFbVcamUymb+iWtmjSvEhkZbWlN75HZJrXyNSVKoVoz79PKoP6Ngw4YgBlrbA4gPLKajLt470+CSBhwUKcPHMXOX0ulwHeiqXGrH69Xr83PT29XZM09QWiV1VV1YWnVSirLLOB6C9tJKrQu1wDHGEF9SPGKyEyAE2d49sAEvOjJZsimUPSlkozGo1n0tLSujYpci08DTqdDo7BVA4eo5JYNlWlsMN2KXjqAHSy0QJIkEYv/nM/i9tZib0ZsbGUE13Izc3t3yQkkXAD9KaG0t8LbYEHy1ga7nrwNBEACevxt3aQiLg8eyAqzMvLG9GoQSSIUHozQ+jvVzjhMdpy4+ty1jcDyBGV9vhqsu1gojUHJAMRlfilWVlZIxulOhOpLeTsFDgCHixt4kfNAHLC3P9h8xlSUVVtFURarbYwMTGxcakzwYysrKy8kf6erAQevd5IyhVuXBP/VjOAnFgDHl1F5q84ZVWdwTqjhktqaGhot0Zh4guOrOTkZJjqkUqcp6xCy/wv2w6et/T7VBcS9ZnHmgHk5EIO079+DKEWmtx7LTy4FRUVUUuWLGnv085GkSu9Bb3wXUrgQZIVvKxI4Po9LEMCoAKijpzUDKB6SaKVZOHKUMVYGkBkMplIcXHxfno2LSVn5XOqC6kY/xX5eepuBLr6C/qk9KU3PPrpdeRsgmVqhUmVSlShd7sHQMf7EH3BniYLIAFEX/x4khgV1Bn8RHq9nhQWFi7GGfmcKhNYvtlsfqauEk/kJNRRzoNcYiF1QglAxopzpCpkuJsAhFDGD00aQII6W/drLNtvqbMRZ6JWq8nFixen+pRlJqCZ6lk4CsukCWAgeFsPnCfDn1xbd6Pjpm4mBZer5AAKHuYxAGmrDeS9BUftxqRsLV8EEfK0fz2STOjDrJigVk5fYWFhA3yCVEt4zylpYBQ3ERp9idz2zDqLm7zvxZ9l+tqtEuiYH6lO/5blEylF429/dj25d9pmMvmD3eStfx9ma+bC38niDZFk6c9RVtfnP4TUvR9eYnzGPc9vYurEmyC65x+bSGR8viyaj7MxGo2koKAggp5ZK6/zIe5baEFJ2nwl3pOSUUImvh4ou0ElABku/06qgvzdBKCeRJvwNqkxWuYjFxSrSGJaMckrqiJXyjXsmjRUMmFV6wxKT7Esoi+8HwYCPqO4VE3SskrJ6dhcsmVfAiO3r3/+G3ni7R1k7HMbPJIBCemI78uXSHksnJFGo4Eq+5bzodZeJc3V1dV3KvEebOib/z7ESLMjANLn73QbeFhGYtxrLEXEG8WFBgq0y1fUJDa5kBwMTmOS7bkPdzNrFDEud6nDz78PVgrAMj5UVlZGoqOj78cZhoSEtPaW6mpJxeIZqeqCi33l9hgSYCVh3RsAQpUGfE0+UKlat4quqMjJqByycFUYSy2Bd9nVJUZrd5xTtMqgyvLz88/RM2zjcVUmqC56ER9JVRf0LnjPqL/9ZPXGvAKg0LuJWZvr/EGjxsxAVYHZ5DYgwfSGVQh1unxbFJk6ey8ZM3m9Q4WO9hbUZnRigYwP4cxUKhVJTk6e61FVxj2ZLQoLC2+hSC7nqqsuKQylvtM+3mfzprwBoMrjtxBTVbI815r+m6HoEFu6rJWkOvXfFkub9H9EE/sy0SZ/Kvt/uvTv6v7WUBJCzLpiYjZpGgwojVbPDh2uj4de2dpAPrScvPHFIaUSItZN5PLlyxUHDx68lYOopae4T0tKMNfJra4a8r/1EaTfg8t9D0B0GYoOyrzfVcGDXfodsCRVYfdS0L1CQfYfos8LJMbSUMpcs+vFwcx0Rcblkc+WBDMLrz58qf/DK5grRRozw9lRDksuXLiwDWfqdrNeIM5VVVVjhQ5eYuKcmnmF3EFNYns3pGiFFe5nUsKdANJlLZNJIFX4X90MXD8K0qGsKEAd9SypvrCAGIqPE5M6s7ZOzWx0mIgnU6sWUunR1wJZfb4zIEJBY9LFYkVCXVJSUkOJtPsJNRdxkD57pdIHInL2ouNMZNYHQO71A4lqw2rMnskAsCepTo4impipFFDzqPoLlhU92lpwFew9nkqefGcn8XeCJ338bZDVMEdaWtoR+IbcpsYE6VNRUfGAkBAvvpDwc7lk0KRVDt0IOlqkXyrznCeaL5Q+oxTaIoktfbFXACSWUCgeUIWOJdrED4m+YDdTd/ZIO1QbTPSjoRnMJTBoov29RzTgfFqxkm/oanFxMaHS5wm3SSGR9DnIVZf5D9JnIO/MP+KEu93DsbA6S+xOFrS1UA1UnVQFD/AyiMSZAxRMpx8g2pS5xFgewwFv26GJhg2/HEoij8zYzviOLUINLoUwjjTMASmUmpp63C1SSJA+paWlY5WchsdPZzrlw/B4NF5QGycCiOHyYcvvVV9sWNGiW8HUh6gjHyW67LWsMtdus6wKLeNId1IJb23vR1ApBN+TEhcqKioiR48efcDlUkiwvIxG41qp9AGzf/nTA07n9Ho0H0gcE0v7Ulb5qo6e4psAEqWjVJ28nWiT5lBJHUfMCo2yxD6lizml5JPFJ8jIp5R9cbMoVzVI6swQANdqtSQhIcG1Fpng96Eky0/J75OQetnpFimCWWkJoCKiPvO42w9DE/O8PJk//X++DSAL8n0bBdLHVGKnsTo6a0Cq1hnJoZCLzPqSEm1w0Hh6bkp+oUuXLlWuW7cuAGe+c+fOVq7yOrfUaDRzpKka0KUffPm7034J/4eXk59+jZV5Rz1hEaEVjNmosnxqyyKZumgsIBLUMZNI5VE2ORKs3aU/n7VIp8FCkNeyV4AZx2umRhKJjIychzOPiYlp44qYF1Zrk8kUI1VfSeklZOyUDfUO9BklZSnapNke2XxD8TFJZ7NiyoPuaVQAEvKcqkJGUuttNlNt1iQSsgaOhqaTZ97/tS48ghQbcCapGkP664ULF87TM7+We6dbNJj7lJSUjBe6oYrJ8+pfYurtYp/x2UFZ5pwu5yePbHz1hYWWDjyTjh1CowOQWCKduoOpYrOhXNnsp9Ie6R3ISffnltrGPfHSlBVGpi9fvkwOHTo0EWffIDLNRVir6urqxVL1hXSNJ97aUW8ATXhtu8ycRE6QJzZbffYpJnUsvrvoN6oW+jdqENW28ruX6HO3Wg2bADC7j6WwsMiEV7eT3MJKqU/IjNTX6Ojo1Tj7eqsxQX2tWLHievrByVLyjI4RI21E3O0tOL6QgGVZG5/MxLL7edBgzh0sy4rgJ2rsAKq9vwFEm/BObVNRBbWGdJuYpAIy9eN9SiZ9DUCWnp6e9tprr3WptxoT1FdRUdEDIvVVZ7ojKcqRsIWtlSiJzZj1V1jcyBObrMv8UV4ZSze9KQDoD8fpXcyHVGNW7ngGlYZmnwqeaeYT2r9//1P1VmP8j1pT6+srKXmG+nrszV8anKsSeChJ0hC8mqqXpz2jxs48Jos/GUpOUDV2a5MCEWritNS6RecTa9xIIT5mRq5QRETEcmDAaQCJrK+2RqMxXGZ9XSxxSW7vvOWnLG+AElvk3XhGzA9iQUyZM9MDvijPr15EHTGBcswjFn0irS0ACBmLSUlJ0RQD7Z1WY0LcKy4u7lb6YVVS62tVA6wv8Xrt899kDSWRP+OWHkFK1ljyZ5J0ChPRZS1vggD6w3eE4DG87/YGxEBo5OTkqCkHHgUsOOVUFKwvas49z/lPjdjD+d6CIy4B0KOvB5JLBZUyp567o/Jir65ZX2pJ5LW5HuNh3grWIq3FpMm0HumvdSrWlJSUkCNHjrwJLFAAtXWW/7ShenAJs+voS/jwnIIKZoK7qnUbmiVZEunS2la+HtpQfeE+Im2zhzRWpW73TWYd782CtGx2iJW0EagxmPOnT5/+CVhwmAdB102ZMgXiqp1erz8hBRDqnVxZPbB+V6zn2rwoxcai/y7LETJcOenyVFffjKuNZv4va6MWwIMojTkNHgRMOMSDBP6zePHirhQ3uZz/1KmwwEOJLi09wbglaQEf2vt6wh8k+IRgfUmtQU3c600eQLUgup1NNFIg1yy4evHixcLp06f34hH6lo6qr1YJCQkYd22UEmg0NHIlgO6dvkUhvTXeozxEE/eG3NlGLbRKNril6YOoMsi/FkQipyN9qBmRzs/PN23YsOE+YGLNmjVtHCXQrS9duvScqJFz3QejftyVABpMeRDa+1vyoDKijvmHBz23w4ipMl7S6FxFtAnv/jkAxAovH6LEOkdKpK+CSO/evfs1YMIhIs0B1La0tPRTzn/q1Be6YN3/0s8ub0ny1ZpwScNIM+ss70k/SXXKXJkYR7ijyTkWrQHo1FiLujkACLKjsrKSHD9+/GtgwiEA8Te1q6qqWiEl0HB9SzttuGJN+XC3TI2ZKs97OH40XDbRsDbF5OOmbZEJ64Q/VdtBilmKYWFhW4AJhwA0b948cKD29A/3SwF0KiqHDH18tVu6a51Puyybc4rkck9uojZxliyKbapMJNrzHzJSjaWOmsxMYCzkcCNBraqJcCXpOAhYYgaDgURFRSHZ/jqODduv+++/H2+6TqfTnQWBFqsw5DEPdtOIyJXbo1mpikV+UMYSjz79IO4yi4yrVGEh38ZcnccWCgGM5dHsb/R521k+DmZ8AFyo+0K4pHbeR+OQYNrzM6UORRaZj6cviomOHBstbEkfmGmtAwICOhmNxlQpgLb/lkgCHnFP8yQ0FFAsNjx5m0c3EXXwrmiYYNbms7Icfd42xq800c/VTlb0kHuifnVzE2QAAgaSkpIyevTogVbBrTlGrPKfVvxNPUwmU57UB/T9pjNua4oEbnU2IV/ujzn3okc9tPBCu7wDh1nPOnyYtHmsDk138RuiPvNE7cPh5nJu57Iab5f5gkBj0tLSCl988cV+HBut7QGozcqVK/0p+kqlAFriRgBh/bjlrDyflzVd8POQKftgbbWDVH1RILOZ8/CTIPgqLDsFfzYlFFWFxooY5jSFy4JZew2di+YmAGVlZZV/8MEHw4ANRwDUdvv27YPo36qlTkR3A2j8tM2yNiRm+tR6hEwH9ZN17xDUKOrGwGvAb6rR5gWLqiVMUgT3QXsXqCvmR2E1W84Dy6TOIPrstUziurtC11EACc7EnJwczRdffHG7wwCiP4dT8Og8DSDUl6GWyVL8m0g16rbcyR3oZ8Ncl1pg+G901LAqAaF+gvow6VEVMoKoTo1hUgzRbqRNoArWVJVEzLrLtVLMLpDMLNUCUhB/D07iybJrawDKzc3VL1iw4G5gwyaAuKsa9v4oCh6DpwGE9eF/f5cl2yO315HR3g2qmddmK6jPXxqmPqlKgpdbfeZJJr2grgAoh9VcdRGVbgdqszRdNb2xHgDKy8szUQCNAzYogNraBVBgYOBobwEIkX4kfMvzld91k+k+SFYvX6u64qhEudNNgL2HaJM/IYbCfTzx3X6PIGPleeaPqn2Q/DwtgYwOAYiLp3abN28eQcFT7Q0AYX29NlzWTctwJcT1iWZUQrAaMUmtOeJgGpZg707y7sfKiOCM1CS8zYwFpups8SejhnEt1PfjsF1Nuq0B6NKlS7rPPvsMKqzdm2++2cYugFavXj2EfoDK01aYsB54eSvJzJUUyBm1Lq+agGqQNXainEt3aYPna8QAZmreO9axzMSS5OE5d+V1KgBIsMLUc+bMGeMwgBYuXHgr/eMr3gIQeix+tTpMLsbLo10WNlCF38/CFHLVFVvrQfZ4LddgYiw97Zz1ZtKzmCHSUVwBJGtmfGZmZtmrr7460i6AhEzEGTNm9DYYDLJkMrT399SMiDsmr2eNkyw3jEqh5H822CJjSWTU9JaqC7QBRts5b5jQmnPTZdkA6Hh/PDyTlVEpld/84VOqoCrw11p3RwP2Bn+vBKCUlJTCiRMnDgQ2eDjDNoDoukmv16dIQxlHTqU73MbOFQueb+n4Ipi4SMVsUNAwZ5282A650JhV74VQQ21WpGWJETjgp98Fszr2lz45wPYe4xdsWm36Msrp5tfbYtXGv60YykhISMigmPgLsMExYvXVsm/fvujK0EWr1UZ6MphqrRknmiVJfSXo51yvEECQP8/7qZZ1a2Wf6aFyIqX4m7TlDCpG7xR1PkEBwuQPdrFucAo9ny3CJlD16IPk7B5pz38gk0Do1hEdHR1HMdGNY8NmWmsLKqLwphvLy8v3eSqdwxYXghSSim80oVRHTnTa6kE6hrx7hZkV3KFNirfKbAySyhBIXSTZKXWqh5vjw/8eYwCzMrW5rmVNddpXTnm1UROn1Dvx5MmTSOfowrFhG0CUJF1Hf95QVFS00lMJZfbCGzKLjHXTOOREUygKnphprPJUTsxjWGNwb8Wf0JBcyn0wmOUuG30OhRyqRT+dVpzII7YokVWJMIxdlwSVVvqCX6UZiay05/Dhw2h7dwPHhs3KjBazZs1CKWvntLS0udKUVkxbRpNrTwIIHABzuaRPm9moIRo4F+1xFtRARU9RBA8yEGtr0LyTrwOuIs0/AvdxtO9S/0dWkIde2UYOBKcpzksVJCwadLKR6jYsNVieKOqUprRiqs+OHTv+B0w4A6BOYWFhU6XdWN2RVO+odzotu1QBAGl2W7Koo59TrMI060rY0+/NZC9N7KsyPpadX8Eqdp3N6ETjqAxJ721pM1Fdznqrzlh0qkXHWmlSfWFhIVm+fPk7wIRDAJo5cyassI5r1669h36QLKAKL7E3pvDNXHiUVCk8ZYaCPcq+ISqZQCQBFHmyVx41m6d5vVkmwhMWD4S51vKq97hLSi8wTl1lg2QbS8NZkFbuVP2bxV4JXujMzEz9J5988igw8corr1xrF0Dczu9wzz339KEEKltqie0NSm1wX6B6lf9Q62/nkWSFJ6uKiuc5MrWFilOT9pKCqVvC+JBXE7kouJFhIJU+mPAI/1dDy6QAwhwqyczWVBqcj0jUE6l/TcL7Fl5wwYSPj4/PHzFiBHxAHW6//fY29gCEV6vu3bt3oD+7V1RUHOcAqiPSUecLbM4Cc+d69LXtpLBEpRC1zrdoUo5+h+Iapz/UVhFLtfB+G7q/ykIoaIQ5f/kpl8wIw2c8+c4OZqmZTMqxNRbvi3ujLp6my1xqzQJDafNfevbseZ0wZ9Xeq+WUKVOupz+7Zmdnf+/O5gr12Zh//RiiNNaaBVsRnoDpKp2NKoAM3l7UgHm3/dxgNplIen1oMjFwomudtBgmjDHgGEVh1fGY+h+WzwR+JLXA0PJ3z549G4AFjgmHRiC0fPLJJ4G2LsHBwS9JiTTau8ysR29oV60RT60lx8Iy5ANxTXpirEpUbOdWS5hf9oH8Yz+iTXiPPv2WpndJucbpbv/OjL1csCJUHhYSBalRziPuYCYQ6Pz8fLJ06dIPgAWOCYcA1GLSpEkg0p1nz5492mQylUljYqjO8OZYa0wkzs4rdyxVlFpqmtgZvlH9GTZOsdUc5pr6P+zeac4YgKzkT7OyWAwsOTm58oUXXrgXWOBORIe6lLUAWerSpUsn+rsfFWOnpEQ6O7/c5eLWKd8QVWWYBWE0mW070SpiGxxgdF13sFtrE9ck/XhwqGOeW+8R9Y+2vsnpJXbHmeOgwX9CQkLQ4u4WYMFRAl1HpMeNG9eR/uyRkpKySAogrc5Annpnp9cAhIwATEbMLaq0OigX3ejxxPtEUR8rF5ojy/WB82/W18ftjgh15b5NfD2QhJzNZi4Da93J4EAsLS0l27dvXwtj6s477+zkKIGu40ETJkyAJdZ148aNT4KRi/1BYPbLfo6yOZvKnZuAIsRISWczi8Boxg8ea5PnUOJa1DOykeM4qC37ElzSrLQ+5BreaysAYv6fjIwM8+effz4dGKDqy2ECXafGAgICwINuoKsvVWMJUikEE3HM5PUeBQ5uHEN9EStS8nHAdGddXoP8fahYbwwxlsvr3bB/yDbwlhQf9fRPrK2OQkC2Bp3JwsLCLtCzxzTnGzgWnGo2zgasjB8//kbwoKSkpCVSAMGURnqBR8BD199n7WHZANb0N5Lga+d++VAd+okAos/7Raa6YMm+9Ml+rxoiQpNTMQ0Qq6+tW7eux9nffffd6Fbf2lkAiXnQzd9///0kao3JqjQw+NWdXmkQP+jsX48mW3eIIcZDzVCf65AR1I9UX1ykOMfrh81nvA4erC8kPjVBfaWlpRmpBf4PnH19+E8dD0IGWseOHW+CGisuLpYlmMFd7g6nIuaJjXluA/nfhggWXFQED32qUWOFhpy+NyQF/p632egGaVf4g5R7eMuTLx36d14y5gDSB9bXiRMn0I2jPz/7ds7yH7Eaa8NFmF9wcPBnyE4T94yGGY0Zna52FKIPYxIzN62Xt6CkGPzC97pc+BFN1GQZacbCxOSHZmzzCekzbc4+Czog9IaG83DlypWLcOZjx44FgNrUR33VqbGhQ4eCgd9MmfioysrKIqlTEeOChj6+poEEeTkZN3Uzm6KHzzNay7Qz64mpIpZozr3E5oj6ZKs49F9GsaDk2guLVeTvH+3xWFGCvfQY5FhLyTN+RkdHl9x111134swHDhzYsb7qy0KNjR49Gr1h+sTHx6+HFBLPzYCUmLskuF6+DJiwd07ZyIKImNyj15us14xr8xinUJ26w2cbNqlO30+MZRGy68d8d8wF8QXwYD317k6L0IYQ+7py5Qp8Pztw1iNGjLi5IerLwhpDwyn6s+enn346Ua1WG6TxsfjUIosEcEeyDP9Gb2LtznPsyTTbbINSxeZnIILty52+wMNMkvwewVoFeJwdTOxO7rP79xRF309CQoLx9ddffxZn7e/v37m+1pdMjaFnYr9+/brTn/6pqan7pVLIaDSTBVT92JM28HsgBBF+LpeUV1VbtayEdAN9wR6ijpzgtWoJhyVPxMOKRYFI0YCzEBUVvgAeLLheihWkDyLve/fuRfK8Pz/r9g1VXxZkmnIhkOleCxcufFqlUpmkJj1U0O3PrrNw/AXQpw7ORkTvfzmUxEp0jDYqCWqbOWlZagYqKKpODPBp4AipoLW5xGZZbvPuY6kNmurojvY5Ow8nK85JTUxMNM+cOROe516DBg1qMHmWAghIvK5Pnz4oLguIi4vbg+6dYikEjyZGeWO8NOJk85adIkdDM8iVcq1jrUz0pczppkK6pZc7dTkXXU9UvJ/NexOYRekr4MFCgaJY6gvSB4nzO3fuPIqz5WcsJI+5BEACmW7bq1cvSKFb3n///UmlpaVaqRTC7K8LWaWkuFRjs2ZJ3MoXlRG67NW1ebqNZuitH6tLUyLMOCBYOCN9DDxIlQ05m6M4Fyw2Nrb6hRdemIyz9fPzg/Rp21DybE0KdRgwYIAfkBoWFrahurraQgo50xQAYl+bMpd1SG9cjbz9WNey2q7ucg4Xl1LEuov4EniwPv8+WOp1ZtIHI743b94My4vaSgEYqtLBVeRZ0aTnCL1lDH3l5uYW4ELE3mnrpLiKldyihYmaks5G2f0d80fPz5R5mIW153gqG6fta+BBzEshl5yVLYeHh18eNWoUmkfd0rNnz66uMN1tmvRAKEdq/8DAwLlg73wiuCKvMVXEsYbhSGlA8+3GO0p7IMu3NuvLFa0txAVdOUfNlU7DQycvWuT/CF7njIwMsnjx4v/iLN0tfSy4EEL87du3701/DkpISAjlqswkjVWh5zI2vtHP1Dp1B3MpKDV/gg9rx5Fkn4hvKWVu/uuHEIvMTa66THjwDxw4EEXPcDA/yxvcwX2sWmR9+/btAZ/BG2+88UReXl41Nw1rLIvYwrzWstZlca2Y51mrXyW+g2qHBStOsVosXwOPYHUVFKsUQxZRUVG6qVOngjj7u8vysiWF4CPoREHUl/4cuGvXrq/hBhcHWoWGUOhX48vt/W3NEAPJVwqKYmFY8CeLg72SUejIQmQgQRJtF/J9Ll68SJYtW/YDzo6fYSd+pi2v8cBLkEKsj1Dnzp3RAn9IREREEDo5SK0y5OuwqohGAyI/NjueNXwyGxRVFiYLwaPrjQpdRzMaEK6QRtuFeNfu3bvDcGb87Lrws/SI9JER6q5du0L89R8+fPjdqamp+cgnkYIIXdjVZ5/yecsL6hYSs9bKMivGtQ6fTCcPUjPdVwKjsjqwR1exyUdiP5wAHp1Oh0qLwsGDB/8VZ0bPrqcniLM9Qt1ZUGXz58+fQU37GkU+VHaGkVHfHIPdh6gjHiGG4t+Vhs/WVVB8u+40Gf30Op8EjpAWg1wqhVYvrM4rPj6+ZtasWe/irCjvgfTp7AnibE+VIeh2U69evQIgFrdt2/Y/tAThs1aviuNcCDhiJICvxbL0OetlLebETy/ifFPn7PVZlSWk/74696CsVo5H2q9euHABVabLcEb8rG4SBUxbXOOlV50qQwJSp06dcGHDDh8+vLO4uFjBS21mncVUoXd5GTi9WKPO6pR/seZLSlxHyOPZvC/B5XNi3QGe1z7/jRF7BdJszsnJIZs2bTqAs+FndLM3VZc1Vdbxuuuu68lLQYZTXRuC7H7cgLw93WHvSaLjt7DBKlCptpp64zBQ9OcreTy2yp2en72XZXIqgQfaYOfOneH0TNDn+db27dsjFNXRm6pLSQq15C7wG+irD3QsXaNOnz59Dg4rOK4sUwjMtd3FHOnd5zKCPIKNaTKpUmyGXMAfMLds5FM/+TRwhM5kr//rN5ZbpQAeEyyuffv2nadngVFNA/nZ3CAKV7S4xkdeYj7UpXv37v7wUgcEBNx95syZixgdLQcRBogk8K4Zvd0Wu0IPRMzDwHA3WyOXkEkQGn2JTP94HwmYsKJRgAdJfMh8UAJPeXk5mmNm+vv7j8dZ8DPp4gu8xx4fgkezKydqg8eNG/cABVGWIImU4mWYXOPSrEOqpjBdGeOVlFrcSf06aHDw8bdBZMjjq30eOEK3tlWBMbKxWGLwHD169NKYMWMexhnQswCt6MrPprUvgscie5ETtG6CZQYQhYeHp1njRCCx6NWsboBKw2A25Ogg4MlaqDBpYz1l1mAwkeSMEhYrgmnuyxaWeKEr6/4TF2TpwALnKSkpgeTJ4OARLK5u/Eza+DJ4pKEOVg7E0T+kf//+44KCgs4h/4SD6KpsAo0mmxFchwKwVMqg2xfMcC21poxXQljlhr1pNwBOek4Z+XJ1GOtB3a+RAAfJ8M99uJu1Z1EAD7wmZtR07d69O6Ffv373cvDcyi2u6z0ZqnAliMD2u/v5+Q2gP4e2adPmtkOHDgXn5uYyPxFcRfJadxPrvCrueWgxY+vkKNaeF6MJEOQ0m7QOJ7IhJ/vrNeFU4vzUKEAjHnOwcFWYzEEoOGzh58nKyiKBgYGhdI/vwF7zPe/Oz6CNr5FmRy2ztvwGenTt2hU3NITe4ChqVv6KXBRe6WqWgchsqu1pSAk2xmNjhoMuey0j3WjhXzvb1LHBtuWV1awKBH2Uxz63gaU4NBbgoJE48ssxsccK32Ftd1JSUsjGjRv30r0djT3me91DYq43GvBYA1F37sQaTG905OrVq79DJYBGo1HmRXyMEUi2Y0PYLNVUGQUOmjJM/799rJVJY5I4QvsVVOpKnYNi8CAPKzo62vzdd98txZ5ib/ked2/s4FECEfRwt2uvvbZfly5dhtLfR3z00Uevh4WF5cJrjQw5ZZXm+IK0QUI7HIB3eLB3katLb1C3Hp96WdZFTKSyasAljx07lv/222+/A/BgT+ne+nPCfL0vOQpdyYk6cHPylh49egBEw+Gn2LNnzwnkqKBUSMnUt7bQKiUrr5yJeCSNPzJje4Pr9L3Jc6bN2UuO0Xspq9BakzrMl5aamooS5JN9+/a9D3vYu3fvYdhTvrcdGiPncVQSteG+CATyelMQDUF8Brxo6dKl38bExGjgPQUpxJNmSxrFJheRd+YdYakVvprU5Uy1KExzpYEpYqIMN0h4eLjm22+/XYI9w97xB7E339PrmiJ4lJyN8Iai+1nPjh07DrjxxhuHQww/88wzf9u/f394WloagX7nJFERSJgHsXTLWfKwj7RLcXahbgwVu5CeSjV0AnCwB5DMkDrU+Ih4/PHHn8VeYc+wd9hDvpft+d62vKaJv8QZjZ044evbvXt3iGEAafQ333zzn9DQ0DyY+4Klhr2UhUKMZtaACtl3L/5zP+vc6o3Gn44GPVEhAcCj2350YoHi5EF+j1cFCysvLw8Nnwrmz5+/gFtZw/le9eV718kbGYW+AiKBXEP89qJP1MAOHTowaUT1+r2bNm36OSoqSlVUVCR00DJZU23gQyjqQ8ePGXMP+lSVxF9f2MIGoARFZFntGC+SOCbocBgWZ86cUa1Zs2Zbr1697sOeYG/oHg3CXvE9E8jynwo8SryoPY8S95BIo1GTJk16etu2bQdiY2P1sDw4kMwCkMwyS6U29bToipqcjMph3UD+MXsva/LgCR8QGrDDwz3js4Pkh81nSURsHssrsqamxKoKwAEHPHfunH7Lli2/TZw48RkFqdOD71X7ps53nJVG7bjvApZEr3bt2g24+eabR8DcxyZOnz59WmBg4GFKtLUQ6+AFIJbW1Ju4Vh1WTVxqETkUcpF8tzGSvDv/CJn0xi8MVOAiCE4iuu1o2ijeD6sJHm2MpnzirR3kw6+OkXW7Yln9eWrmFZszu8RqCiY51HRBQQF8OtqtW7cemTp16gscOCOwB9gLLnW68j1q92eVOo5Ko85cv98CkU03EU6ykdhUkMh169b9fOrUqXx4s1UqFVDEylV4WdFVR3xJaDODcl+kqx4LyyS7KI9C99Qlm2wv9ITE+0POZLMo/uVStc0+RxJpc5VLG5ajDCcqQhC4l7Vr12597LHHJnPgjMQ9c3XVh+9F52ap4xw36sCtC4jsPtdff/1gWB6CRKIbPO7rr7/+klptUVS9Mamk1WoJF0s1IjV3tSHOyfouCWDMAmhQGQFpExcXp8W1f/nll//t1q3beEHi4B5xrxw4PfgedPgzc536SqPWXFRfzxOhUD7Ul0skQbWNouv2yZMnT12+fPmaI0eOJMTHx+tgvUEy8WIEC0CJQaXEoeoBkjqw8CYTFoCBdISkAWjOnz+vwzXiWp999tlpuHbuz2Gqikucvvxeu/B7bycyz5vB4wIgMYkEXkCJ5XBqmUC1MfWGSPS0adNe/PHHH1fs3bv3dERERDF8SsgLBqDANUQv4aDNImlVI4BA+F3p3wQcCn+LzxI+FGACYED409PTCa5h37594bgmyuNeptc4hl8rrKqRuAfOcQSJIwDn2mbguB5I1/LNvZHnuYBY+lORPwRPcNu2bQWphAO6bdiwYRNmz579wapVq9bu2rULef4ZVN1V4WCh8mDxAFhQK7zvI7H3gkTBe1FAib9Fdy9Il8zMTNRdVeE78F34zlmzZn04dOjQR7mkwTWNwjXiWnHNuHZ+Dzfze2qWOB4CUlvutu/ELZO/8FhQfwFMIslUByi67nrooYee/uijj2YuWrTom/Xr12/evn37oQMHDkSeOHEiOTQ0NDsyMjKfmtElFAxlVOVUJCYmViQkJJRR4JWcPXs2PywsLCs4ODj54MGDkb/88sshfMY333yzCJ+Jz27duvXd/LtG8+9mkkYEmv78Wv/Cr70zv5e2zcDxLJBacYtEkEqdRWBCbKgfVQ0DKSkdhsOjPwXeJAXVbVxCjMHh+/n5PUClxsSxY8c+Pn78+Cex8Dv+Df+PA2Qs1KXo7+vAwonwCP6dw3ANuBZ+TWLQCGqqDb+XZuB4AUhSqSQG003c/O3Jn3gA6lZuzTFQUUCMgrksklbiNUqyLP4/lyoj+WcwsOCz8R0cMLfw7+7Or0UMGqm0aQaOj4LpOu54u4GT026cqPpxidCHHza4SH8cPiQGgMDXEL4Gc3AM5ADpz/+mH/+M3vwze/Dv6MK/syO/hmbQNFIwteKH1oaT0/bcp3I9508CsG7iqqUbJ7TdORjEqzv/f934e28SAaUT/8wO/Dva8e9sLVFPzaBp5IASg0oAVlt+4Nfyw2/PpYbSEv7/tfxv2oqAIgZLM2D+JKCSgqulBGjipfQe6ef86V7/D5Bo3rUKRHewAAAAAElFTkSuQmCC\">\n" + "    <style>\n" + "    html * {\n" + "      font-family: 'Roboto', sans-serif;\n" + "      font-size: 12px !important;\n" + "    }\n" + "    .vbr-body {\n" + String.format(
                "      color: %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                            R.color.colorOnScoreSheetBackground))) + "      width: 28cm;\n" + "      max-width: 28cm;\n" + "      margin-left: auto;\n" + "      margin-right: auto;\n" + "    }\n" + "    .vbr-captain {\n" + "      text-decoration: underline;\n" + "    }\n" + "    .vbr-home-team {\n" + String.format(
                "      color: %s;\n", homeTeamColor) + String.format("      background-color: %s;\n",
                                                                     homeTeamBackgroundColor) + "    }\n" + "    .vbr-home-libero {\n" + String.format(
                "      color: %s;\n", homeLiberoColor) + String.format("      background-color: %s;\n",
                                                                       homeLiberoBackgroundColor) + "    }\n" + "    .vbr-guest-team {\n" + String.format(
                "      color: %s;\n", guestTeamColor) + String.format("      background-color: %s;\n",
                                                                      guestTeamBackgroundColor) + "    }\n" + "    .vbr-guest-libero {\n" + String.format(
                "      color: %s;\n", guestLiberoColor) + String.format("      background-color: %s;\n",
                                                                        guestLiberoBackgroundColor) + "    }\n" + "    .div-card {\n" + String.format(
                "      background-color: %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                                       R.color.colorScoreSheetBackground))) + "      padding: 6px;\n" + "      margin: 6px;\n" + "      box-shadow: 0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24);\n" + "    }\n" + "    .div-title {\n" + "      font-weight: 700;\n" + "      margin-bottom: 6px;\n" + "    }\n" + "    .div-grid-h-g {\n" + "      display: grid;\n" + "      grid-template-columns: 4fr 1fr 4fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-1-2-3 {\n" + "      display: grid;\n" + "      grid-template-columns: 4fr 1fr 4fr 1fr 4fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-game-header-logo {\n" + "      display: grid;\n" + "      grid-template-columns: 4fr 6fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-game-header-info {\n" + "      display: grid;\n" + "      grid-template-columns: 40fr 20fr 20fr 8fr 12fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-sets-info {\n" + "      display: grid;\n" + "      grid-template-columns: 40fr 5fr 5fr 5fr 5fr 5fr 5fr 10fr 20fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-flex-row {\n" + "      display: flex;\n" + "      flex-flow: row wrap;\n" + "      align-items: flex-start;\n" + "      align-content: flex-start;\n" + "      justify-content: flex-start;\n" + "    }\n" + "    .div-flex-column {\n" + "      display: flex;\n" + "      flex-flow: column wrap;\n" + "      align-items: flex-start;\n" + "      align-content: flex-start;\n" + "      justify-content: flex-start;\n" + "    }\n" + "    .div-grid-team {\n" + "      display: grid;\n" + "      grid-template-columns: 1fr 8fr 1fr 8fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: center;\n" + "      align-content: start;\n" + "      justify-content: start;\n" + "      justify-items: start;\n" + "    }\n" + "    .div-grid-set-header-info {\n" + "      min-width: 175px;\n" + "      display: grid;\n" + "      grid-template-columns: 3fr 1fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-set-header-time {\n" + "      min-width: 250px;\n" + "      display: grid;\n" + "      grid-template-columns: 7fr 3fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .set-index-cell {\n" + "      grid-row: 1 / span 2;\n" + "      line-height:44px;\n" + "    }\n" + "    .div-grid-lineup {\n" + "      display: grid;\n" + "      grid-template-columns: 1fr 1fr 1fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: center;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-substitution {\n" + "      display: grid;\n" + "      grid-template-columns: 24fr 16fr 24fr 34fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: center;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-timeout {\n" + "      display: grid;\n" + "      grid-template-columns: 1fr 2fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: center;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-sanction {\n" + "      display: grid;\n" + "      grid-template-columns: 3fr 2fr 4fr;\n" + "      grid-auto-rows: 1fr;\n" + "      align-items: center;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-grid-signature {\n" + "      display: grid;\n" + "      grid-template-columns: 2fr 5fr;\n" + "      align-items: stretch;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "    }\n" + "    .div-footer {\n" + "      font-size: 10px;\n" + "      position: fixed;\n" + "      display: flex;\n" + "      flex-flow: row wrap;\n" + "      align-items: center;\n" + "      align-content: center;\n" + "      justify-content: center;\n" + "      bottom: 12px;\n" + "      right: 12px;\n" + "    }\n" + "    .vbr-logo-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.mipmap.ic_launcher_round, 128,
                                                                                         128)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 16px;\n" + "      height: 16px;\n" + "      margin-left: 6px;\n" + "    }\n" + "    .cell {\n" + "      min-width: 22px;\n" + "      text-align: center;\n" + "      padding: 3px;\n" + "    }\n" + "    .bordered-cell {\n" + String.format(
                "      border: 1px solid %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                                       R.color.colorOnScoreSheetBackground))) + "      min-width: 22px;\n" + "      text-align: center;\n" + "      padding: 3px;\n" + "      margin-right: -1px;\n" + "      margin-left: -1px;\n" + "    }\n" + "    .remarks-cell {\n" + String.format(
                "      border: 1px solid %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                                       R.color.colorOnScoreSheetBackground))) + "      padding: 3px;\n" + "      min-height: 40px;\n" + "    }\n" + "    .signature-title-cell {\n" + String.format(
                "      border: 1px solid %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                                       R.color.colorOnScoreSheetBackground))) + "      width: inherit;\n" + "      grid-row: 1 / span 2;\n" + "      height:82px;\n" + "      text-align: center;\n" + "      padding: 3px;\n" + "      margin-right: -1px;\n" + "      margin-left: -1px;\n" + "    }\n" + "    .signature-name-cell {\n" + String.format(
                "      border: 1px solid %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                                       R.color.colorOnScoreSheetBackground))) + "      width: inherit;\n" + "      height:14px;\n" + "      line-height:14px;\n" + "      text-align: center;\n" + "      padding: 3px;\n" + "      margin-right: -1px;\n" + "      margin-left: -1px;\n" + "    }\n" + "    .signature-cell {\n" + String.format(
                "      border: 1px solid %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                                       R.color.colorOnScoreSheetBackground))) + "      width: inherit;\n" + "      height: 60px;\n" + "      line-height: 60px;\n" + "      text-align: center;\n" + "      padding: 3px;\n" + "      margin-right: -1px;\n" + "      margin-left: -1px;\n" + "    }\n" + "    .signature-image {\n" + "      width: auto;\n" + "      height: 100%;\n" + "    }\n" + "    .logo-image {\n" + "      width: auto;\n" + "      height: 60px;\n" + "      padding: 3px;\n" + "      margin-right: auto;\n" + "      margin-left: auto;\n" + "    }\n" + "    .set-anchor {\n" + String.format(
                "      color: %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                            R.color.colorOnScoreSheetBackground))) + "    }\n" + "    .badge {\n" + "      min-width: 22px;\n" + "      text-align: center;\n" + "      padding: 3px;\n" + "      margin: 2px;\n" + "      border-radius: 5px;\n" + "    }\n" + "    .spacing-before {\n" + "      margin-top: 12px;\n" + "    }\n" + "    .ladder-spacing {\n" + "      margin-bottom: 10px;\n" + "    }\n" + "    .horizontal-spacing {\n" + "      min-width: 34px;\n" + "    }\n" + "    .border {\n" + String.format(
                "      border: 1px solid %s;\n", colorIntToHtml(ContextCompat.getColor(mContext,
                                                                                       R.color.colorOnScoreSheetBackground))) + "      margin-right: -1px;\n" + "      margin-left: -1px;\n" + "    }\n" + "    .substitution-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.ic_thumb_substitution, 42,
                                                                                         32)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: auto;\n" + "      height: 20px;\n" + "    }\n" + "    .service-gray-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.ic_thumb_service, 32,
                                                                                         32)) + "      display: inline-block;\n" + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 10px;\n" + "      min-width: 10px;\n" + "      height: 10px;\n" + "    }\n" + "    .service-white-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.ic_thumb_service_white, 32,
                                                                                         32)) + "      display: inline-block;\n" + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 10px;\n" + "      min-width: 10px;\n" + "      height: 10px;\n" + "    }\n" + "    .timeout-gray-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.ic_thumb_timeout, 32,
                                                                                         32)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: auto;\n" + "      min-width: 12px;\n" + "      height: 12px;\n" + "    }\n" + "    .timeout-white-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.ic_thumb_timeout_white, 32,
                                                                                         32)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: auto;\n" + "      min-width: 12px;\n" + "      height: 12px;\n" + "    }\n" + "    .yellow-card-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.yellow_card, 64,
                                                                                         64)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 24px;\n" + "      height: 24px;\n" + "    }\n" + "    .red-card-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.red_card, 64,
                                                                                         64)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 24px;\n" + "      height: 24px;\n" + "    }\n" + "    .expulsion-card-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.expulsion_card, 96,
                                                                                         64)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 36px;\n" + "      height: 24px;\n" + "    }\n" + "    .disqualification-card-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.disqualification_card, 128,
                                                                                         64)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 48px;\n" + "      height: 24px;\n" + "    }\n" + "    .delay-warning-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.delay_warning, 64,
                                                                                         64)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 24px;\n" + "      height: 24px;\n" + "    }\n" + "    .delay-penalty-image {\n" + String.format(
                "      background-image: url(\"data:image/png;base64,%s\");\n", toBase64(R.drawable.delay_penalty, 64,
                                                                                         64)) + "      background-repeat: no-repeat;\n" + "      background-size: 100% 100%;\n" + "      width: 24px;\n" + "      height: 24px;\n" + "    }\n" + "    .new-page-for-printers {\n" + "      break-before: page;\n" + "    }\n" + "    </style> \n" + "    <style type=\"text/css\" media=\"print\">\n" + "    body {\n" + "      -webkit-print-color-adjust: exact;\n" + "    }\n" + "    </style>" + "  </head>\n" + "  <body class=\"vbr-body\">\n" + "  </body>\n" + "</html>\n";
    }

}