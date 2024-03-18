package android.print;

import android.content.*;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.webkit.*;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.scoresheet.ScoreSheetBuilder;
import com.tonkar.volleyballreferee.ui.util.*;

public class ScoreSheetPdfConverter {

    private final ProgressIndicatorActivity mActivity;
    private final Uri                       mFileUri;
    private       WebView                   mWebView;

    public ScoreSheetPdfConverter(ProgressIndicatorActivity activity, Uri fileUri) {
        mActivity = activity;
        mFileUri = fileUri;
    }

    public void convert(ScoreSheetBuilder.ScoreSheet scoreSheet) {
        mActivity.showProgressIndicator();
        mWebView = new WebView(mActivity);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                PrintDocumentAdapter printAdapter = mWebView.createPrintDocumentAdapter(scoreSheet.filename());

                PrintAttributes printAttributes = new PrintAttributes.Builder()
                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                        .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();

                print(printAdapter, printAttributes);

                mWebView = null;
            }
        });

        mWebView.loadDataWithBaseURL(null, scoreSheet.content(), "text/html", "UTF-8", null);
    }

    private void print(PrintDocumentAdapter printAdapter, PrintAttributes printAttributes) {
        printAdapter.onLayout(null, printAttributes, null, new PrintDocumentAdapter.LayoutResultCallback() {
            @Override
            public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                printAdapter.onWrite(new PageRange[]{ PageRange.ALL_PAGES }, getOutputFile(), new CancellationSignal(),
                                     new PrintDocumentAdapter.WriteResultCallback() {
                                         @Override
                                         public void onWriteFinished(PageRange[] pages) {
                                             super.onWriteFinished(pages);

                                             if (!mActivity.isFinishing()) {
                                                 mActivity.hideProgressIndicator();

                                                 Intent intent = new Intent();
                                                 intent.setAction(Intent.ACTION_VIEW);
                                                 intent.setDataAndType(mFileUri, "application/pdf");
                                                 intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                 try {
                                                     mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(
                                                             R.string.create_score_sheet)));
                                                 } catch (ActivityNotFoundException e) {
                                                     Log.e(Tags.SCORE_SHEET, "Exception while opening the score sheet", e);
                                                     UiUtils
                                                             .makeErrorText(mActivity, mActivity.getString(R.string.score_sheet_exception),
                                                                            Toast.LENGTH_LONG)
                                                             .show();
                                                 }
                                             }
                                         }
                                     });
            }
        }, null);
    }

    private ParcelFileDescriptor getOutputFile() {
        ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = mActivity.getContentResolver().openFileDescriptor(mFileUri, "rw");
        } catch (Exception e) {
            Log.e(Tags.SCORE_SHEET, "Exception while creating the parcel file descriptor", e);
            parcelFileDescriptor = null;
        }
        return parcelFileDescriptor;
    }
}
