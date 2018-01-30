package com.tonkar.volleyballreferee.ui.data;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.PdfGameWriter;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.io.File;

public abstract class RecordedGameActivity extends AppCompatActivity {

    protected RecordedGamesService mRecordedGamesService;
    protected long                 mGameDate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recorded_game, menu);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            MenuItem shareMenu = menu.findItem(R.id.action_share_game);
            shareMenu.setVisible(false);

            MenuItem pdfMenu = menu.findItem(R.id.action_generate_pdf);
            pdfMenu.setVisible(false);
        }

        MenuItem recordMenu = menu.findItem(R.id.action_record_game);

        if (PrefUtils.isPrefOnlineRecordingEnabled(this)) {
            if (mRecordedGamesService.isGameRecordedOnline(mGameDate)) {
                recordMenu.setIcon(R.drawable.ic_record_ok_menu);
            } else {
                recordMenu.setIcon(R.drawable.ic_record_on_menu);
            }
        } else {
            recordMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record_game:
                uploadGame();
                return true;
            case R.id.action_generate_pdf:
                generatePdf();
                return true;
            case R.id.action_delete_game:
                deleteGame();
                return true;
            case R.id.action_share_game:
                shareGame();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uploadGame() {
        Log.i("VBR-RGameActivity", "Upload game");
        if (!mRecordedGamesService.isGameRecordedOnline(mGameDate)) {
            mRecordedGamesService.uploadRecordedGameOnline(mGameDate);
            invalidateOptionsMenu();
        }
    }

    private void generatePdf() {
        Log.i("VBR-RGameActivity", "Generate PDF");
        File file = PdfGameWriter.writeRecordedGame(this, mRecordedGamesService.getRecordedGameService(mGameDate));
        if (file == null) {
            Toast.makeText(this, getResources().getString(R.string.report_exception), Toast.LENGTH_LONG).show();
        } else {
            Uri uri = FileProvider.getUriForFile(this, "com.tonkar.volleyballreferee.fileprovider", file);
            grantUriPermission("com.tonkar.volleyballreferee.fileprovider", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(uri,"application/pdf");
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = Intent.createChooser(target, file.getName());
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e("VBR-RGameActivity", "Exception while showing PDF", e);
                Toast.makeText(this, getResources().getString(R.string.report_exception), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void deleteGame() {
        Log.i("VBR-RGameActivity", "Delete game");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_game)).setMessage(getResources().getString(R.string.delete_game_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mRecordedGamesService.deleteRecordedGame(mGameDate);
                Toast.makeText(RecordedGameActivity.this, getResources().getString(R.string.deleted_game), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(RecordedGameActivity.this, RecordedGamesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void shareGame() {
        Log.i("VBR-RGameActivity", "Share game");
        UiUtils.shareRecordedGame(this, mRecordedGamesService.getRecordedGameService(mGameDate));
    }

    protected String buildScore(RecordedGameService recordedGameService) {
        StringBuilder builder = new StringBuilder();
        builder.append("\t\t");
        for (int setIndex = 0; setIndex < recordedGameService.getNumberOfSets(); setIndex++) {
            int homePoints = recordedGameService.getPoints(TeamType.HOME, setIndex);
            int guestPoints = recordedGameService.getPoints(TeamType.GUEST, setIndex);
            builder.append(String.valueOf(homePoints)).append('-').append(String.valueOf(guestPoints)).append("\t\t");
        }

        return builder.toString();
    }

}
