package com.tonkar.volleyballreferee.business.history;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

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

    private static Font defaultFont = new Font(Font.FontFamily.HELVETICA, 11);

    private Context             mContext;
    private RecordedGameService mRecordedGameService;
    private Document            mDocument;
    private Font                mHomeTeamFont;
    private Font                mHomeCaptainFont;
    private Font                mGuestTeamFont;
    private Font                mGuestCaptainFont;
    private Font                mHomeLiberoFont;
    private Font                mGuestLiberoFont;

    private BaseColor           mHomeTeamColor;
    private BaseColor           mGuestTeamColor;
    private BaseColor           mHomeLiberoColor;
    private BaseColor           mGuestLiberoColor;

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
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
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

    private void init() {
        mDocument.addAuthor("Volleyball Referee");
        mDocument.addCreator("Volleyball Referee");

        mHomeTeamFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getTeamColor(TeamType.HOME))));
        mHomeCaptainFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), Font.UNDERLINE, new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getTeamColor(TeamType.HOME))));
        mHomeTeamColor = new BaseColor(mRecordedGameService.getTeamColor(TeamType.HOME));

        mHomeLiberoFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getLiberoColor(TeamType.HOME))));
        mHomeLiberoColor = new BaseColor(mRecordedGameService.getLiberoColor(TeamType.HOME));

        mGuestTeamFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getTeamColor(TeamType.GUEST))));
        mGuestCaptainFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), Font.UNDERLINE, new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getTeamColor(TeamType.GUEST))));
        mGuestTeamColor = new BaseColor(mRecordedGameService.getTeamColor(TeamType.GUEST));

        mGuestLiberoFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(UiUtils.getTextColor(mContext, mRecordedGameService.getLiberoColor(TeamType.GUEST))));
        mGuestLiberoColor = new BaseColor(mRecordedGameService.getLiberoColor(TeamType.GUEST));
    }

    private void writeRecordedIndoorGame() throws DocumentException {
        writeRecordedGameHeader();
        writeRecordedIndoorTeams();

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            if (setIndex %2 == 1) {
                mDocument.newPage();
            }
            writeRecordedSetHeader(setIndex);
            writeRecordedStartingLineup(setIndex);
            writeRecordedSubstitutions(setIndex);
            writeRecordedLadder(setIndex);
        }
    }

    private void writeRecordedGameHeader() throws DocumentException {
        PdfPTable dateTable = new PdfPTable(1);
        dateTable.setSpacingBefore(5.f);

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        PdfPCell dateCell = new PdfPCell(new Phrase(formatter.format(new Date(mRecordedGameService.getGameDate())), defaultFont));
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dateTable.addCell(dateCell);

        float[] columnWidths = {0.4f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.3f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10.f);

        PdfPCell homeTeamNameCell = new PdfPCell(new Phrase(mRecordedGameService.getTeamName(TeamType.HOME), mHomeTeamFont));
        homeTeamNameCell.setBackgroundColor(mHomeTeamColor);
        homeTeamNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamNameCell);

        PdfPCell homeTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getSets(TeamType.HOME)), defaultFont));
        homeTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamSetsCell);

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.HOME, setIndex)), defaultFont));
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

        PdfPCell guestTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getSets(TeamType.GUEST)), defaultFont));
        guestTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        guestTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(guestTeamSetsCell);

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mRecordedGameService.getPoints(TeamType.GUEST, setIndex)), defaultFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        startIndex = mRecordedGameService.getNumberOfSets();

        for (int setIndex = startIndex; setIndex < 6; setIndex++) {
            PdfPCell cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        mDocument.add(table);
    }

    private void writeRecordedIndoorTeams() throws DocumentException {
        float[] columnWidths = {0.15f, 0.85f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10.f);

        PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.players), defaultFont));
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

    private PdfPTable createTeamTable(TeamType teamType) {
        int numColumns = 20;
        PdfPTable table = new PdfPTable(numColumns);
        table.setWidthPercentage(100);

        for (int player: mRecordedGameService.getPlayers(teamType)) {
            PdfPCell cell = createPlayerCell(teamType, player, false);
            table.addCell(cell);
        }

        for (int libero: mRecordedGameService.getLiberos(teamType)) {
            PdfPCell cell = createPlayerCell(teamType, libero, true);
            table.addCell(cell);
        }

        int startIndex = mRecordedGameService.getPlayers(teamType).size() + mRecordedGameService.getLiberos(teamType).size();

        for (int index=startIndex; (index % numColumns != 0); index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" ", defaultFont));
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        return table;
    }

    private void writeRecordedSetHeader(int setIndex) throws DocumentException {
        float[] columnWidths = {0.15f, 0.15f, 0.15f, 0.55f};
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

        PdfPCell indexCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), mContext.getResources().getString(R.string.set_number), (setIndex+1)), defaultFont));
        indexCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        indexCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(indexCell);

        int homeScore = mRecordedGameService.getPoints(TeamType.HOME, setIndex);
        int guestScore = mRecordedGameService.getPoints(TeamType.GUEST, setIndex);
        PdfPCell scoreCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%d-%d", homeScore, guestScore), font));
        scoreCell.setBackgroundColor(color);
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        int duration = (int) Math.ceil(mRecordedGameService.getSetDuration(setIndex) / 60000.0);
        PdfPCell durationCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), mContext.getResources().getString(R.string.set_duration), duration), defaultFont));
        durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(durationCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(emptyCell);

        mDocument.add(table);
    }

    private void writeRecordedStartingLineup(int setIndex) throws DocumentException {
        float[] columnWidths = {0.15f, 0.15f, 0.7f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.confirm_lineup_title), defaultFont));
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

    private PdfPTable createLineupTable(TeamType teamType, int setIndex) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        PdfPCell pos4TitleCell = new PdfPCell(new Phrase("IV", defaultFont));
        pos4TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos4TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos4TitleCell);

        PdfPCell pos3TitleCell = new PdfPCell(new Phrase("III", defaultFont));
        pos3TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos3TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos3TitleCell);

        PdfPCell pos2TitleCell = new PdfPCell(new Phrase("II", defaultFont));
        pos2TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos2TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos2TitleCell);

        PdfPCell pos4Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_4, setIndex), false);
        table.addCell(pos4Cell);

        PdfPCell pos3Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_3, setIndex), false);
        table.addCell(pos3Cell);

        PdfPCell pos2Cell = createPlayerCell(teamType, mRecordedGameService.getPlayerAtPositionInStartingLineup(teamType, PositionType.POSITION_2, setIndex), false);
        table.addCell(pos2Cell);

        PdfPCell pos5TitleCell = new PdfPCell(new Phrase("V", defaultFont));
        pos5TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos5TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos5TitleCell);

        PdfPCell pos6TitleCell = new PdfPCell(new Phrase("VI", defaultFont));
        pos6TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos6TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos6TitleCell);

        PdfPCell pos1TitleCell = new PdfPCell(new Phrase("I", defaultFont));
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
        float[] columnWidths = {0.15f, 0.85f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.substitutions_tab), defaultFont));
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

    private PdfPTable createSubstitutionsTable(TeamType teamType, int setIndex) {
        float[] columnWidths = {0.166f, 0.166f, 0.166f, 0.166f, 0.166f, 0.166f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Substitution> substitutions = mRecordedGameService.getSubstitutions(teamType, setIndex);

        for (int index = 0; index < 12; index++) {
            PdfPCell cell;
            if (index < substitutions.size()) {
                cell = new PdfPCell(createSubstitutionTable(teamType, substitutions.get(index)));
            } else {
                cell = new PdfPCell();
            }
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        return table;
    }

    private PdfPTable createSubstitutionTable(TeamType teamType, Substitution substitution) {
        float[] columnWidths = {0.22f, 0.15f, 0.22f, 0.39f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        table.addCell(createPlayerCell(teamType, substitution.getPlayerIn(), false));

        PdfPCell arrowCell = new PdfPCell(new Phrase("=>", defaultFont));
        arrowCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        arrowCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(arrowCell);

        table.addCell(createPlayerCell(teamType, substitution.getPlayerOut(), false));

        PdfPCell scoreCell = new PdfPCell(new Phrase(substitution.getHomeTeamPoints() + "-" + substitution.getGuestTeamPoints(), defaultFont));
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        return table;
    }

    private void writeRecordedLadder(int setIndex) throws DocumentException {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase(mContext.getResources().getString(R.string.points_tab), defaultFont));
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
            PdfPCell cell = new PdfPCell(new Phrase(" ", defaultFont));
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
            guestCell = new PdfPCell(new Phrase(" ", defaultFont));
        } else {
            homeCell = new PdfPCell(new Phrase(" ", defaultFont));
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
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(player), font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5.f);
        cell.setBackgroundColor(color);
        return cell;
    }

    private void writeRecordedBeachGame() throws DocumentException {
        writeRecordedGameHeader();

        for (int setIndex = 0; setIndex < mRecordedGameService.getNumberOfSets(); setIndex++) {
            writeRecordedSetHeader(setIndex);
            writeRecordedLadder(setIndex);
        }
    }

}
