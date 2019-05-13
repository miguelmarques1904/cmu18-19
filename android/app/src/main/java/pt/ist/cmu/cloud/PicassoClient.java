package pt.ist.cmu.cloud;

import android.content.Context;

import com.dropbox.core.v2.DbxClientV2;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

/**
 * Singleton instance of Picasso
 */
public class PicassoClient {
    private static Picasso sPicasso;

    public static void init(Context context, DbxClientV2 dbxClient) {
        sPicasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(context))
                // .addRequestHandler(new FileThumbnailRequestHandler(dbxClient))
                .build();
    }

    public static Picasso getPicasso() {
        return sPicasso;
    }
}
