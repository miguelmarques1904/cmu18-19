package pt.ist.cmu.helpers;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageHelper {
    public static Bitmap getBitmapFromURI(ContentResolver cr, Uri uri) {

        Bitmap bitmap = null;
        try {
            InputStream inputStream = cr.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return bitmap;
    }
}
