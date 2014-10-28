package com.collegecode.mymusic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by saurabh on 14-10-26.
 */
public class PaymentActivity extends BaseActivity {
    public static String PAY_URL_KEY = "payment.url.key";
    private Context context;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_payment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        WebView wb = (WebView) findViewById(R.id.wb_pay);
        getSupportActionBar().setTitle("Payment");
        wb.setWebViewClient(new wbClient());
        wb.loadUrl(getIntent().getStringExtra(PAY_URL_KEY));
    }

    private class wbClient extends WebViewClient{
        ProgressDialog dialog;

        @Override
        public void onPageStarted(final WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            dialog = new ProgressDialog(context);
            dialog.setMessage("Please Wait...");
            dialog.setIndeterminate(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    view.stopLoading();
                    finish();
                }
            });
            dialog.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(dialog.isShowing())
                dialog.dismiss();
        }
    }
}
