package pt.ist.cmu.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageHelper {
    public static Bitmap getBitmapFromURI(Context context, Uri uri) {

        File file = UriHelper.getFileForUri(context, uri);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return bitmap;
    }
}
