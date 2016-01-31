package com.slim.slimfilemanager.services.dropbox;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

public class DropboxLoginActivity extends Activity {

    DropboxAPI<AndroidAuthSession> mAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppKeyPair appKeyPair = new AppKeyPair(
                DropBoxConstants.ACCESS_KEY, DropBoxConstants.ACCESS_SECRET);
        mAPI = new DropboxAPI<>(new AndroidAuthSession(appKeyPair));

        SharedPreferences prefs = getSharedPreferences(DropBoxConstants.DROPBOX_NAME, 0);
        String key = prefs.getString(DropBoxConstants.ACCESS_KEY, null);
        String secret = prefs.getString(DropBoxConstants.ACCESS_SECRET, null);

        if (key == null && secret == null) {
            mAPI.getSession().startOAuth2Authentication(this);
        }
    }

    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = mAPI.getSession();
        int resultCode = RESULT_CANCELED;
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();

                String accessToken = session.getOAuth2AccessToken();
                if (accessToken != null) {
                    SharedPreferences.Editor editor =
                            getSharedPreferences(DropBoxConstants.DROPBOX_NAME, 0).edit();
                    editor.putString(DropBoxConstants.ACCESS_TOKEN, session.getOAuth2AccessToken());
                    editor.apply();
                }

                resultCode = RESULT_OK;

            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication",
                        Toast.LENGTH_SHORT).show();
            }
        }
        setResult(resultCode);
        finish();
    }
}
