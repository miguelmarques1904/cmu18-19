package pt.ist.cmu.cloud;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dropbox.core.android.Auth;
import com.orhanobut.hawk.Hawk;

import pt.ist.cmu.helpers.Constants;

public abstract class DropboxActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        String accessToken = null;
        if (!Hawk.contains(Constants.DROPBOX_TOKEN_KEY)) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                Hawk.put(Constants.DROPBOX_TOKEN_KEY, accessToken);
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(Hawk.get(Constants.DROPBOX_TOKEN_KEY).toString());
        }

        String uid = Auth.getUid();
        String storedUid = Hawk.get(Constants.DROPBOX_UID_KEY);
        if (uid != null && !uid.equals(storedUid)) {
            Hawk.put(Constants.DROPBOX_UID_KEY, uid);
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        loadData();
    }

    protected abstract void loadData();

    protected boolean hasToken() {
        return Hawk.contains(Constants.DROPBOX_TOKEN_KEY);
    }

}
