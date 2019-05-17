package pt.ist.cmu.helpers;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {
    public static Bitmap getBitmapFromURI(ContentResolver cr, Uri uri) {

        Bitmap bitmap = null;
        try {
            InputStream inputStream = cr.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
            } catch (IOException eio) {
                eio.printStackTrace();
            }
            return null;
        }

        return bitmap;
    }
}
