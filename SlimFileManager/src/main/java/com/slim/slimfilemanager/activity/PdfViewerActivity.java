package com.slim.slimfilemanager.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.slim.slimfilemanager.R;

import java.io.File;

public class PdfViewerActivity extends Activity {

    private PDFView mPDFView;

    private int mPageCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pdf);

        mPDFView = (PDFView) findViewById(R.id.pdf_view);

        parseIntent(getIntent());
    }

    private void parseIntent(Intent intent) {
        final String action = intent.getAction();
        final String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action)
                || Intent.ACTION_EDIT.equals(action)
                || Intent.ACTION_PICK.equals(action)
                && type != null) {
            Uri uri = intent.getData();
            File newFile = new File(uri.getPath());
            setFile(newFile);
        }
    }

    private void setFile(File file) {
        mPDFView.fromFile(file)
                .defaultPage(0)
                .showMinimap(true)
                .enableSwipe(true)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        mPageCount = nbPages;
                    }
                })
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {

                    }
                })
                .load();
    }
}
