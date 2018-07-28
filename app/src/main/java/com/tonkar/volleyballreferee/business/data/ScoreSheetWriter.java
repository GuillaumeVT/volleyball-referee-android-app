package com.tonkar.volleyballreferee.business.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ScoreSheetWriter {

    private Context             mContext;
    private RecordedGameService mRecordedGameService;
    private Document            mDocument;

    public static File writeRecordedGame(Context context, RecordedGameService recordedGameService) {
        File file;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
                formatter.setTimeZone(TimeZone.getDefault());
                String date = formatter.format(new Date(recordedGameService.getGameSchedule()));

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
                switch (recordedGameService.getGameType()) {
                    case INDOOR:
                        pdfGameWriter.writeRecordedIndoorGame();
                        break;
                    case BEACH:
                        pdfGameWriter.writeRecordedBeachGame();
                        break;
                    case INDOOR_4X4:
                        pdfGameWriter.writeRecordedIndoor4x4Game();
                        break;
                    case TIME:
                        pdfGameWriter.writeRecordedTimeGame();
                        break;
                }
            } catch (IOException e) {
                Log.e("VBR-PDF", "Exception while writing game", e);
                file = null;
            }
        }
        else {
            file = null;
        }

        return file;
    }

    private ScoreSheetWriter(Context context, RecordedGameService recordedGameService, Document document) {
        mContext = context;
        mRecordedGameService = recordedGameService;
        mDocument = document;
    }

    private String htmlSkeleton(String title, String icon) {
        return "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">" +
                "    <title>" + title + "</title>" +
                "    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css\" integrity=\"sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB\" crossorigin=\"anonymous\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "    <meta name=\"theme-color\" content=\"#1f4294\">" +
                "    <link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"favicon.ico\">" +
                "  </head>\n" +
                "  <body class=\"vbr-body\">\n" +
                "  </body>\n" +
                "</html>\n";
    }
}
