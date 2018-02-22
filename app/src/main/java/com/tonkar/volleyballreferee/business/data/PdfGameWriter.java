package com.tonkar.volleyballreferee.business.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.Timeout;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PdfGameWriter {

    static {
        FontFactory.register("/system/fonts/Roboto-Regular.ttf", "Roboto");
    }

    private Context             mContext;
    private RecordedGameService mRecordedGameService;
    private Document            mDocument;

    private Font mDefaultFont;
    private Font mHomeTeamFont;
    private Font mHomeCaptainFont;
    private Font mGuestTeamFont;
    private Font mGuestCaptainFont;
    private Font mHomeLiberoFont;
    private Font mGuestLiberoFont;

    private BaseColor mHomeTeamColor;
    private BaseColor mGuestTeamColor;
    private BaseColor mHomeLiberoColor;
    private BaseColor mGuestLiberoColor;

    private Image mSubstitutionImage;
    private Image mTimeoutGrayImage;
    private Image mTimeoutWhiteImage;
    private Image mYellowCardImage;
    private Image mRedCardImage;
    private Image mExpulsionCardImage;
    private Image mDisqualificationCardImage;
    private Image mDelayWarningImage;
    private Image mDelayPenaltyImage;

    public static File writeRecordedGame(Context context, RecordedGameService recordedGameService) {
        File file;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
                formatter.setTimeZone(TimeZone.getDefault());
                String date = formatter.format(new Date(recordedGameService.getGameDate()));

                String homeTeam = recordedGameService.getTeamName(TeamType.HOME);
                String guestTeam = recordedGameService.getTeamName(TeamType.GUEST);

                String filename = String.format(Locale.getDefault(), "%s_%s_%s.pdf", homeTeam, guestTeam, date);
                File filedir = new File(Environment.getExternalStorageDirectory(),"Documents");
                filedir.mkdirs();
                file = new File(filedir, filename);
                Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                PdfGameWriter pdfGameWriter = new PdfGameWriter(context, recordedGameService, document);
                pdfGameWriter.init();
                if (GameType.INDOOR.equals(recordedGameService.getGameType())) {
                    pdfGameWriter.writeRecordedIndoorGame();
                } else {
                    pdfGameWriter.writeRecordedBeachGame();
                }
                document.close();
            } catch (IOException | DocumentException e) {
                Log.e("VBR-PDF", "Exception while writing game", e);
                file = null;
            }
        }
        else {
            file = null;
        }

        return file;
    }

    private PdfGameWriter(Context context, RecordedGameService recordedGameService, Document document) {
        mContext = context;
        mRecordedGameService = recordedGameService;
        mDocument = document;
    }

    private void init() throws IOException, BadElementException {
        mDocument.addAuthor("Volleyball Referee");
        mDocument.addCreator("Volleyball Referee");

        mDefaultFont = FontFactory.getFont("Roboto", BaseFont.IDENTITY_H, true, 10);

        int homeTeamColor = mRecordedGameService.getTeamColor(TeamType.HOME);
        int guestTeamColor = mRecordedGameService.getTeamColor(TeamType.GUEST);

        if (homeTeamColor == guestTeamColor) {
            guestTeamColor = ContextCompat.getColor(mContext, R.color.colorReportDuplicate);
        }

        mHomeTeamFont = FontFactory.getFont("Roboto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, homeTeamColor)));
        mHomeCaptainFont = FontFactory.getFont("Roboto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), Font.UNDERLINE, new BaseColor(UiUtils.getTextColor(mContext, homeTeamColor)));
        mHomeTeamColor = new BaseColor(homeTeamColor);

        mHomeLiberoFont = FontFactory.getFont("Roboto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getLiberoColor(TeamType.HOME))));
        mHomeLiberoColor = new BaseColor(mRecordedGameService.getLiberoColor(TeamType.HOME));

        mGuestTeamFont = FontFactory.getFont("Roboto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, guestTeamColor)));
        mGuestCaptainFont = FontFactory.getFont("Roboto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), Font.UNDERLINE, new BaseColor(UiUtils.getTextColor(mContext, guestTeamColor)));
        mGuestTeamColor = new BaseColor(guestTeamColor);

        mGuestLiberoFont = FontFactory.getFont("Roboto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getLiberoColor(TeamType.GUEST))));
        mGuestLiberoColor = new BaseColor(mRecordedGameService.getLiberoColor(TeamType.GUEST));

        mSubstitutionImage = Image.getInstance(convertToBytes(R.drawable.ic_thumb_substitution, 32, 32));
        mTimeoutGrayImage = Image.getInstance(convertToBytes(R.drawable.ic_thumb_timeout, 32, 32));
        mTimeoutWhiteImage = Image.getInstance(convertToBytes(R.drawable.ic_thumb_timeout_white, 32, 32));
        mYellowCardImage = Image.getInstance(convertToBytes(R.drawable.yellow_card, 50, 50));
        mRedCardImage = Image.getInstance(convertToBytes(R.drawable.red_card, 50, 50));
        mExpulsionCardImage = Image.getInstance(convertToBytes(R.drawable.expulsion_card, 75, 50));
        mDisqualificationCardImage = Image.getInstance(convertToBytes(R.drawable.disqualification_card, 120, 100));
        mDelayWarningImage = Image.getInstance(convertToBytes(R.drawable.delay_warning, 50, 50));
        mDelayPenaltyImage = Image.getInstance(convertToBytes(R.drawable.delay_penalty, 50, 50));

        mSubstitutionImage.scaleAbsolute(12, 12);
        mTimeoutGrayImage.scaleAbsolute(12, 12);
        mTimeoutWhiteImage.scaleAbsolute(12, 12);
        mYellowCardImage.scaleAbsolute(16, 16);
        mRedCardImage.scaleAbsolute(16, 16);
        mExpulsionCardImage.scaleAbsolute(24, 16);
        mDisqualificationCardImage.scaleAbsolute(32, 16);
        mDelayWarningImage.scaleAbsolute(16, 16);
        mDelayPenaltyImage.scaleAbsolute(16, 16);
    }

    private void writeRecordedIndoorGame() throws DocumentException {
        writeRecordedGameHeader();
        writeRecordedIndoorTeams();

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            if (setIndex %2 == 1) {
                mDocument.newPage();
            }
            writeRecordedIndoorSetHeader(setIndex);
            writeRecordedStartingLineup(setIndex);
            writeRecordedSubstitutions(setIndex);
            writeRecordedTimeouts(setIndex);
            writeRecordedSanctions(setIndex);
            writeRecordedLadder(setIndex);
        }
    }

    private void writeRecordedGameHeader() throws DocumentException {
        float[] dateAndLeagueWidths = {0.4f, 0.15f, 0.45f};
        PdfPTable dateAndLeagueTable = new PdfPTable(dateAndLeagueWidths);
        dateAndLeagueTable.setWidthPercentage(100);
        dateAndLeagueTable.setSpacingBefore(5.f);

        PdfPCell leagueCell = new PdfPCell(new Phrase(mRecordedGameService.getLeagueName(), mDefaultFont));
        leagueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        leagueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dateAndLeagueTable.addCell(leagueCell);

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        PdfPCell dateCell = new PdfPCell(new Phrase(formatter.format(new Date(mRecordedGameService.getGameDate())), mDefaultFont));
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dateAndLeagueTable.addCell(dateCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        dateAndLeagueTable.addCell(emptyCell);

        mDocument.add(dateAndLeagueTable);

        float[] columnWidths = {0.4f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.3f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10.f);

        PdfPCell homeTeamNameCell = new PdfPCell(new Phrase(mRecordedGameService.getTeamName(TeamType.HOME), mHomeTeamFont));
        homeTeamNameCell.setBackgroundColor(mHomeTeamColor);
        homeTeamNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamNameCell);

        PdfPCell homeTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getSets(TeamType.HOME)), mDefaultFont));
        homeTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamSetsCell);

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.HOME, setIndex)), mDefaultFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        int startIndex = mRecordedGameService.getNumberOfSets();

        for (int setIndex = startIndex; setIndex < 6; setIndex++) {
            PdfPCell cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        PdfPCell guestTeamNameCell = new PdfPCell(new Phrase(mRecordedGameService.getTeamName(TeamType.GUEST), mGuestTeamFont));
        guestTeamNameCell.setBackgroundColor(mGuestTeamColor);
        guestTeamNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        guestTeamNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(guestTeamNameCell);

        PdfPCell guestTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getSets(TeamType.GUEST)), mDefaultFont));
        guestTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        guestTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(guestTeamSetsCell);

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.GUEST, setIndex)), mDefaultFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        startIndex = mRecordedGameService.getNumberOfSets();

        for (int setIndex = startIndex; setIndex < 6; setIndex++) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        mDocument.add(table);
    }

    private void writeRecordedIndoorTeams() throws DocumentException {
        if (UsageType.NORMAL.equals(mRecordedGameService.getUsageType())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);
            table.setSpacingAfter(10.f);

            PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.players), mDefaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setRowspan(2);
            table.addCell(titleCell);

            PdfPCell homeTeamTable = new PdfPCell(createTeamTable(TeamType.HOME));
            homeTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(homeTeamTable);

            PdfPCell guestTeamTable = new PdfPCell(createTeamTable(TeamType.GUEST));
            guestTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(guestTeamTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createTeamTable(TeamType teamType) {
        int numColumns = 20;
        PdfPTable table = new PdfPTable(numColumns);
        table.setWidthPercentage(100);

        for (int player: mRecordedGameService.getPlayers(teamType)) {
            table.addCell(createPlayerCell(teamType, player, mRecordedGameService.isLibero(teamType, player)));
        }

        int startIndex = mRecordedGameService.getPlayers(teamType).size();

        for (int index=startIndex; (index % numColumns != 0); index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" ", mDefaultFont));
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        return table;
    }

    private void writeRecordedIndoorSetHeader(int setIndex) throws DocumentException {
        float[] columnWidths = {0.15f, 0.05f, 0.05f, 0.05f, 0.7f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20.f);

        Font font;
        BaseColor color;

        if (mRecordedGameService.getPoints(TeamType.HOME, setIndex) > mRecordedGameService.getPoints(TeamType.GUEST, setIndex)) {
            font = mHomeTeamFont;
            color = mHomeTeamColor;
        } else {
            font = mGuestTeamFont;
            color = mGuestTeamColor;
        }

        List<TeamType> ladder = mRecordedGameService.getPointsLadder(setIndex);
        int hScore1 = 0, hScore2 = 0, gScore1 = 0, gScore2 = 0;
        int hScore = 0, gScore = 0;
        boolean partial1Reached = false, partial2Reached = false;

        for (TeamType teamType : ladder) {
            if (TeamType.HOME.equals(teamType)) {
                hScore++;
            } else {
                gScore++;
            }

            if ((hScore == 8 && !partial1Reached) || (gScore == 8 && !partial1Reached)) {
                hScore1 = hScore;
                gScore1 = gScore;
                partial1Reached = true;
            } else if ((hScore == 16 && !partial2Reached) || (gScore == 16 && !partial2Reached)) {
                hScore2 = hScore;
                gScore2 = gScore;
                partial2Reached = true;
            }
        }

        PdfPCell indexCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), mContext.getResources().getString(R.string.set_number), (setIndex+1)), font));
        indexCell.setBackgroundColor(color);
        indexCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        indexCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(indexCell);

        PdfPCell hScoreCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.HOME, setIndex)), mDefaultFont));

        hScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScoreCell);

        PdfPCell hScore1Cell = new PdfPCell(new Phrase(String.valueOf(hScore1), mDefaultFont));
        hScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore1Cell);

        PdfPCell hScore2Cell = new PdfPCell(new Phrase(String.valueOf(hScore2), mDefaultFont));
        hScore2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore2Cell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setRowspan(2);
        table.addCell(emptyCell);

        int duration = (int) Math.ceil(mRecordedGameService.getSetDuration(setIndex) / 60000.0);
        PdfPCell durationCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), mContext.getResources().getString(R.string.set_duration), duration), mDefaultFont));
        durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(durationCell);

        PdfPCell gScoreCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.GUEST, setIndex)), mDefaultFont));
        gScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScoreCell);

        PdfPCell gScore1Cell = new PdfPCell(new Phrase(String.valueOf(gScore1), mDefaultFont));
        gScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore1Cell);

        PdfPCell gScore2Cell = new PdfPCell(new Phrase(String.valueOf(gScore2), mDefaultFont));
        gScore2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore2Cell);

        mDocument.add(table);
    }

    private void writeRecordedStartingLineup(int setIndex) throws DocumentException {
        if (UsageType.NORMAL.equals(mRecordedGameService.getUsageType())) {
            float[] columnWidths = {0.15f, 0.15f, 0.7f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.confirm_lineup_title), mDefaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setColspan(2);
            table.addCell(titleCell);

            PdfPCell ladderCell = new PdfPCell();
            ladderCell.setRowspan(2);
            ladderCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(ladderCell);

            PdfPCell homeTeamTable = new PdfPCell(createLineupTable(TeamType.HOME, setIndex));
            homeTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(homeTeamTable);

            PdfPCell guestTeamTable = new PdfPCell(createLineupTable(TeamType.GUEST, setIndex));
            guestTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(guestTeamTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createLineupTable(TeamType teamType, int setIndex) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        PdfPCell pos4TitleCell = new PdfPCell(new Phrase("IV", mDefaultFont));
        pos4TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos4TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos4TitleCell);

        PdfPCell pos3TitleCell = new PdfPCell(new Phrase("III", mDefaultFont));
        pos3TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos3TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos3TitleCell);

        PdfPCell pos2TitleCell = new PdfPCell(new Phrase("II", mDefaultFont));
        pos2TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos2TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos2TitleCell);

        PdfPCell pos4Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_4, setIndex), false);
        table.addCell(pos4Cell);

        PdfPCell pos3Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_3, setIndex), false);
        table.addCell(pos3Cell);

        PdfPCell pos2Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_2, setIndex), false);
        table.addCell(pos2Cell);

        PdfPCell pos5TitleCell = new PdfPCell(new Phrase("V", mDefaultFont));
        pos5TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos5TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos5TitleCell);

        PdfPCell pos6TitleCell = new PdfPCell(new Phrase("VI", mDefaultFont));
        pos6TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos6TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos6TitleCell);

        PdfPCell pos1TitleCell = new PdfPCell(new Phrase("I", mDefaultFont));
        pos1TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos1TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos1TitleCell);

        PdfPCell pos5Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_5, setIndex), false);
        table.addCell(pos5Cell);

        PdfPCell pos6Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_6, setIndex), false);
        table.addCell(pos6Cell);

        PdfPCell pos1Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_1, setIndex), false);
        table.addCell(pos1Cell);

        return table;
    }

    private void writeRecordedSubstitutions(int setIndex) throws DocumentException {
        if (UsageType.NORMAL.equals(mRecordedGameService.getUsageType())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.substitutions_tab), mDefaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setRowspan(2);
            table.addCell(titleCell);

            PdfPCell homeSubstitutionsTable = new PdfPCell(createSubstitutionsTable(TeamType.HOME, setIndex));
            homeSubstitutionsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(homeSubstitutionsTable);

            PdfPCell guestSubstitutionsTable = new PdfPCell(createSubstitutionsTable(TeamType.GUEST, setIndex));
            guestSubstitutionsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(guestSubstitutionsTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createSubstitutionsTable(TeamType teamType, int setIndex) {
        float[] columnWidths = {0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Substitution> substitutions = mRecordedGameService.getSubstitutions(teamType, setIndex);

        for (int index = 0; index < (2 * columnWidths.length); index++) {
            PdfPCell cell;
            if (index < substitutions.size()) {
                cell = new PdfPCell(createSubstitutionTable(teamType, substitutions.get(index)));
                cell.setBorder(Rectangle.NO_BORDER);
            } else if (index < columnWidths.length) {
                cell = new PdfPCell(new Phrase(" "));
            } else {
                cell = new PdfPCell();
            }
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        return table;
    }

    private PdfPTable createSubstitutionTable(TeamType teamType, Substitution substitution) {
        float[] columnWidths = {0.22f, 0.15f, 0.22f, 0.39f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        table.addCell(createPlayerCell(teamType, substitution.getPlayerIn(), false));

        PdfPCell imageCell = new PdfPCell(mSubstitutionImage);
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imageCell.setPadding(1.f);
        table.addCell(imageCell);

        table.addCell(createPlayerCell(teamType, substitution.getPlayerOut(), false));

        String score;
        if (TeamType.HOME.equals(teamType)) {
            score = substitution.getHomeTeamPoints() + "-" + substitution.getGuestTeamPoints();
        } else {
            score = substitution.getGuestTeamPoints() + "-" + substitution.getHomeTeamPoints();
        }

        PdfPCell scoreCell = new PdfPCell(new Phrase(score, mDefaultFont));
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        return table;
    }

    private void writeRecordedTimeouts(int setIndex) throws DocumentException {
        if (UsageType.NORMAL.equals(mRecordedGameService.getUsageType()) || UsageType.POINTS_SCOREBOARD.equals(mRecordedGameService.getUsageType())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.timeouts_tab), mDefaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(titleCell);

            PdfPCell timeoutsTable = new PdfPCell(createTimeoutsTable(setIndex));
            timeoutsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(timeoutsTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createTimeoutsTable(int setIndex) {
        float[] columnWidths = {0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.3336f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Timeout> hTimeouts = mRecordedGameService.getCalledTimeouts(TeamType.HOME, setIndex);
        List<Timeout> gTimeouts = mRecordedGameService.getCalledTimeouts(TeamType.GUEST, setIndex);

        for (Timeout timeout: hTimeouts) {
            PdfPCell cell = new PdfPCell(createTimeoutTable(TeamType.HOME, timeout));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (Timeout timeout: gTimeouts) {
            PdfPCell cell = new PdfPCell(createTimeoutTable(TeamType.GUEST, timeout));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (int index = hTimeouts.size() + gTimeouts.size(); index < columnWidths.length; index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" "));
            table.addCell(cell);
        }

        return table;
    }

    private PdfPTable createTimeoutTable(TeamType teamType, Timeout timeout) {
        float[] columnWidths = {0.38f, 0.62f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        table.addCell(createTimeoutCell(teamType));

        String score;
        if (TeamType.HOME.equals(teamType)) {
            score = timeout.getHomeTeamPoints() + "-" + timeout.getGuestTeamPoints();
        } else {
            score = timeout.getGuestTeamPoints() + "-" + timeout.getHomeTeamPoints();
        }

        PdfPCell scoreCell = new PdfPCell(new Phrase(score, mDefaultFont));
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        return table;
    }

    private void writeRecordedSanctions(int setIndex) throws DocumentException {
        if (UsageType.NORMAL.equals(mRecordedGameService.getUsageType())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.sanctions_tab), mDefaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(titleCell);

            PdfPCell sanctionsTable = new PdfPCell(createSanctionsTable(setIndex));
            sanctionsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(sanctionsTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createSanctionsTable(int setIndex) {
        float[] columnWidths = {0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Sanction> hSanctions = mRecordedGameService.getGivenSanctions(TeamType.HOME, setIndex);
        List<Sanction> gSanctions = mRecordedGameService.getGivenSanctions(TeamType.GUEST, setIndex);

        for (Sanction sanction: hSanctions) {
            PdfPCell cell = new PdfPCell(createSanctionTable(TeamType.HOME, sanction));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (Sanction sanction: gSanctions) {
            PdfPCell cell = new PdfPCell(createSanctionTable(TeamType.GUEST, sanction));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (int index = hSanctions.size() + gSanctions.size(); index < (2 * columnWidths.length); index++) {
            PdfPCell cell;

            if (index < columnWidths.length) {
                cell = new PdfPCell(new Phrase(" "));
            } else {
                cell = new PdfPCell();
            }
            table.addCell(cell);
        }

        return table;
    }

    private PdfPTable createSanctionTable(TeamType teamType, Sanction sanction) {
        float[] columnWidths = {0.38f, 0.22f, 0.4f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        PdfPCell imageCell = new PdfPCell(getSanctionImage(sanction.getSanctionType()));
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imageCell.setPadding(1.f);
        table.addCell(imageCell);

        int player = sanction.getPlayer();
        table.addCell(createPlayerCell(teamType, player, mRecordedGameService.isLibero(teamType, player)));

        String score;
        if (TeamType.HOME.equals(teamType)) {
            score = sanction.getHomeTeamPoints() + "-" + sanction.getGuestTeamPoints();
        } else {
            score = sanction.getGuestTeamPoints() + "-" + sanction.getHomeTeamPoints();
        }

        PdfPCell scoreCell = new PdfPCell(new Phrase(score, mDefaultFont));
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        return table;
    }

    private Image getSanctionImage(SanctionType sanctionType) {
        Image image;

        switch (sanctionType) {
            case YELLOW:
                image = mYellowCardImage;
                break;
            case RED:
                image = mRedCardImage;
                break;
            case RED_EXPULSION:
                image = mExpulsionCardImage;
                break;
            case RED_DISQUALIFICATION:
                image = mDisqualificationCardImage;
                break;
            case DELAY_WARNING:
                image = mDelayWarningImage;
                break;
            case DELAY_PENALTY:
            default:
                image = mDelayPenaltyImage;
                break;
        }

        return image;
    }

    private void writeRecordedLadder(int setIndex) throws DocumentException {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.points_tab), mDefaultFont));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleTable.addCell(titleCell);

        mDocument.add(titleTable);

        int numColumns = 35;
        PdfPTable table = new PdfPTable(numColumns);
        table.setWidthPercentage(100);

        int homeScore = 0;
        int guestScore = 0;

        for (TeamType teamType: mRecordedGameService.getPointsLadder(setIndex)) {
            PdfPCell ladderCell;
            if (TeamType.HOME.equals(teamType)) {
                homeScore++;
                ladderCell = new PdfPCell(createLadderCell(teamType, homeScore));
            } else {
                guestScore++;
                ladderCell = new PdfPCell(createLadderCell(teamType, guestScore));
            }

            ladderCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(ladderCell);
        }

        int startIndex = mRecordedGameService.getPointsLadder(setIndex).size();

        for (int index = startIndex; (index % numColumns != 0); index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" ", mDefaultFont));
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        mDocument.add(table);
    }

    private PdfPTable createLadderCell(TeamType teamType, int score) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell homeCell;
        PdfPCell guestCell;

        if (TeamType.HOME.equals(teamType)) {
            homeCell = new PdfPCell(createPlayerCell(score, mHomeTeamFont, mHomeTeamColor));
            guestCell = new PdfPCell(new Phrase(" ", mGuestTeamFont));
        } else {
            homeCell = new PdfPCell(new Phrase(" ", mHomeTeamFont));
            guestCell = new PdfPCell(createPlayerCell(score, mGuestTeamFont, mGuestTeamColor));
        }

        table.addCell(homeCell);
        table.addCell(guestCell);

        return table;
    }

    private PdfPCell createPlayerCell(TeamType teamType, int player, boolean isLibero) {
        Font font;
        BaseColor color;

        if (isLibero) {
            font = TeamType.HOME.equals(teamType) ? mHomeLiberoFont : mGuestLiberoFont;
            color = TeamType.HOME.equals(teamType) ? mHomeLiberoColor : mGuestLiberoColor;
        } else {
            if (mRecordedGameService.getCaptain(teamType) == player) {
                font = TeamType.HOME.equals(teamType) ? mHomeCaptainFont : mGuestCaptainFont;
            } else {
                font = TeamType.HOME.equals(teamType) ? mHomeTeamFont : mGuestTeamFont;
            }
            color = TeamType.HOME.equals(teamType) ? mHomeTeamColor : mGuestTeamColor;
        }
        return createPlayerCell(player, font, color);
    }

    private PdfPCell createPlayerCell(int player, Font font, BaseColor color) {
        String playerStr = String.valueOf(player);

        if (player < 0) {
            playerStr = " ";
        } else if (player == 0) {
            playerStr = mContext.getResources().getString(R.string.coach_abbreviation);
        }

        PdfPCell cell = new PdfPCell(new Phrase(playerStr, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5.f);
        cell.setBackgroundColor(color);
        return cell;
    }

    private PdfPCell createTimeoutCell(TeamType teamType) {
        BaseColor backgroundColor;

        if (TeamType.HOME.equals(teamType)) {
            backgroundColor = mHomeTeamColor;
        } else {
            backgroundColor = mGuestTeamColor;
        }

        PdfPCell cell = new PdfPCell(getTimeoutImage(backgroundColor));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2.f);
        cell.setBackgroundColor(backgroundColor);
        return cell;
    }

    private Image getTimeoutImage(BaseColor backgroundColor) {
        Image image;

        double a = 1 - ( 0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue()) / 255;

        if (a < 0.5) {
            image = mTimeoutGrayImage;
        } else {
            image = mTimeoutWhiteImage;
        }

        return image;
    }

    private byte[] convertToBytes(int id, int widthPixels, int heightPixels) {
        Drawable drawable = ContextCompat.getDrawable(mContext, id);
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        return stream.toByteArray();
    }

    private void writeRecordedBeachGame() throws DocumentException {
        writeRecordedGameHeader();

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            writeRecordedBeachSetHeader(setIndex);
            writeRecordedTimeouts(setIndex);
            writeRecordedSanctions(setIndex);
            writeRecordedLadder(setIndex);
        }
    }

    private void writeRecordedBeachSetHeader(int setIndex) throws DocumentException {
        float[] columnWidths = {0.15f, 0.05f, 0.05f, 0.75f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20.f);

        Font font;
        BaseColor color;

        if (mRecordedGameService.getPoints(TeamType.HOME, setIndex) > mRecordedGameService.getPoints(TeamType.GUEST, setIndex)) {
            font = mHomeTeamFont;
            color = mHomeTeamColor;
        } else {
            font = mGuestTeamFont;
            color = mGuestTeamColor;
        }

        List<TeamType> ladder = mRecordedGameService.getPointsLadder(setIndex);
        int hScore1 = 0, gScore1 = 0;
        int hScore = 0, gScore = 0;

        for (TeamType teamType : ladder) {
            if (TeamType.HOME.equals(teamType)) {
                hScore++;
            } else {
                gScore++;
            }

            if (hScore + gScore == 21) {
                hScore1 = hScore;
                gScore1 = gScore;
            }
        }

        PdfPCell indexCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), mContext.getResources().getString(R.string.set_number), (setIndex+1)), font));
        indexCell.setBackgroundColor(color);
        indexCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        indexCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(indexCell);

        PdfPCell hScoreCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.HOME, setIndex)), mDefaultFont));

        hScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScoreCell);

        PdfPCell hScore1Cell = new PdfPCell(new Phrase(String.valueOf(hScore1), mDefaultFont));
        hScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore1Cell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setRowspan(2);
        table.addCell(emptyCell);

        int duration = (int) Math.ceil(mRecordedGameService.getSetDuration(setIndex) / 60000.0);
        PdfPCell durationCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), mContext.getResources().getString(R.string.set_duration), duration), mDefaultFont));
        durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(durationCell);

        PdfPCell gScoreCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.GUEST, setIndex)), mDefaultFont));
        gScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScoreCell);

        PdfPCell gScore1Cell = new PdfPCell(new Phrase(String.valueOf(gScore1), mDefaultFont));
        gScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore1Cell);

        mDocument.add(table);
    }
}
