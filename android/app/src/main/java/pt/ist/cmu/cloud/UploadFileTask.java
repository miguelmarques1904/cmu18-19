package pt.ist.cmu.cloud;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import pt.ist.cmu.helpers.UriHelper;

/**
 * Async task to upload a file to a directory
 */
public class UploadFileTask extends AsyncTask<String, Void, String> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(String link);
        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(String link) {
        super.onPostExecute(link);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (link == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(link);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String localUri = params[0];
        File localFile = UriHelper.getFileForUri(mContext, Uri.parse(localUri));

        if (localFile != null) {
            String remoteFolderPath = params[1];

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            try (InputStream inputStream = new FileInputStream(localFile)) {
                // upload file to dropbox
                mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);

                // return downloadable link
                String url = mDbxClient.sharing().createSharedLinkWithSettings(remoteFolderPath + "/" + remoteFileName).getUrl();
                return url.substring(0, url.length() - 1) + "1";
            } catch (DbxException | IOException e) {
                mException = e;
            }
        }

        return null;
    }
}
